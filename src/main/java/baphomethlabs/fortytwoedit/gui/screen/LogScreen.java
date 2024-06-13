package baphomethlabs.fortytwoedit.gui.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;

public class LogScreen extends GenericScreen {

    EditBoxWidget box;
    static File logFile;
    static final List<LogMessage> FULL_LOG = new ArrayList<>();
    static boolean paused = false;
    long lastCheck = 0;
    long lastUpdate = 0;
    final int UPDATE_WAIT_MS = 1000;
    static final String ss = "\u00a7";
    
    public LogScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.LOG_SCREEN;

        logFile = new File(client.runDirectory.getAbsolutePath()+"\\logs\\latest.log");

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Resume"), Text.literal("Pause")).initially(paused).omitKeyText().build(x+5+40+5,y+5,40,20, Text.of(""), (button, trackOutput) -> {
            paused = (boolean)trackOutput;
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Temporarily freeze new messages from appearing")));
        box = this.addDrawableChild(new EditBoxWidget(this.client.textRenderer, x+15-3, y+35, 240-24, 22*6, Text.of(""), Text.of("")));
        updateBox();
    }

    protected void btnBack() {
        client.setScreen(new SecretScreen());
    }

    protected void updateBox() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<FULL_LOG.size(); i++) {
            sb.append(FULL_LOG.get(i).line()).append("\n\n");
        }
        box.setText(sb.toString());
    }

    private record LogMessage(LogType type, String source, String line) {
        public static LogMessage build(String inpLine) {
            StringBuilder fullLine = new StringBuilder();
            StringBuilder line = new StringBuilder(inpLine);
            LogType logType = LogType.UNKNOWN;
            String source = "";

            int i = line.indexOf("]");
            if(i>2 && i<line.length()-1) {
                String time = line.substring(0,i+1);
                line.delete(0,i+2);
                fullLine.append(ss).append("9").append(time).append(ss).append("r ");
            }

            i = line.indexOf("]");
            if(i>2 && i<line.length()-1) {
                String type = line.substring(0,i+1);
                line.delete(0,i+2);

                if(type.endsWith("INFO]"))
                    logType = LogType.INFO;
                else if(type.endsWith("WARN]"))
                    logType = LogType.WARN;
                else if(type.endsWith("ERROR]"))
                    logType = LogType.ERROR;
                else if(type.endsWith("DEBUG]"))
                    logType = LogType.DEBUG;
                else if(type.endsWith("FATAL]"))
                    logType = LogType.FATAL;

                fullLine.append(ss).append(getTypeColor(logType)).append(type).append(ss).append("r ");
            }

            i = line.indexOf(")");
            if(i>2 && i<line.length()-1) {
                String src = line.substring(0,i+1);
                line.delete(0,i+2);

                if(src.charAt(0)=='(' && src.charAt(src.length()-1)==')')
                    source = src.substring(1,src.length()-1);
                else
                    source = src;

                fullLine.append(ss).append("3").append(src).append(ss).append("r ");
            }

            fullLine.append(line.toString().replace("\t","  "));            

            return new LogMessage(logType, source, fullLine.toString());
        }
    }

    private enum LogType {
        INFO,
        WARN,
        ERROR,
        DEBUG,
        FATAL,

        UNKNOWN
    }
    private static String getTypeColor(LogType type) {
        switch(type) {
            case INFO: return "2";
            case WARN: return "6";
            case ERROR: return "c";
            case DEBUG: return "a";
            case FATAL: return "4";
            case UNKNOWN: break;
        }
        return "f";
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Output Log"), this.width / 2, y+11, TEXT_COLOR);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawBackground(context, delta, mouseX, mouseY, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(FortytwoEdit.magickGuiKey.matchesKey(keyCode,scanCode) || client.options.inventoryKey.matchesKey(keyCode,scanCode)) {
            this.client.setScreen(null);
            return true;
        }
        if(super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if(!paused) {
            if(System.currentTimeMillis()-lastCheck >= UPDATE_WAIT_MS) {
                lastCheck = System.currentTimeMillis();
                if(logFile.lastModified()>lastUpdate) {
                    lastUpdate = logFile.lastModified();

                    try(BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                        String line;
                        int i = 0;
                        while((line = reader.readLine()) != null) {
                            if(i>=FULL_LOG.size()) {
                                FULL_LOG.add(LogMessage.build(line));
                            }
                            i++;
                        }
                    }
                    catch(Exception e) {}

                    updateBox();
                }
            }
        }

        super.tick();
    }

}
