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

    private EditBoxWidget box;
    private static File logFile;
    private static final List<LogMessage> FULL_LOG = new ArrayList<>();
    private static final List<LogMessage> MOD_LOG = new ArrayList<>();
    private static final List<LogMessage> MOD_LOG_QUEUE = new ArrayList<>();

    private static boolean clearFullLogCache = false;
    private static boolean paused = false;
    private static boolean onlyMod = false;

    private long lastCheck = 0;
    private long lastUpdate = 0;
    private static final int UPDATE_WAIT_MS = 1000;
    private static final String ss = "\u00a7";
    
    public LogScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.LOG_SCREEN;

        logFile = new File(client.runDirectory.getAbsolutePath()+"\\logs\\latest.log");

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> this.btnBack()).dimensions(x+5,y+5,40,20).build());
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Resume"),
                Text.literal("Pause")).initially(paused).omitKeyText().build(x+5+40+5,y+5,40,20, Text.of(""), (button, trackOutput) -> {
            paused = (boolean)trackOutput;
            updateBox();
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Temporarily freeze new messages from appearing")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("[42edit]"),
                Text.literal("[All]")).initially(onlyMod).omitKeyText().build(x+backgroundWidth-5-60,y+5,60,20, Text.of(""), (button, trackOutput) -> {
            onlyMod = (boolean)trackOutput;
            updateBox();
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
        List<LogMessage> logList = onlyMod ? MOD_LOG : FULL_LOG;

        for(int i=0; i<logList.size(); i++) {
            if(i>0) {
                sb.append("\n");
                if(logList.get(i).timestamp() != null)
                    sb.append("\n");
            }
            sb.append(logList.get(i).formattedLine());
        }

        box.setText(sb.toString());
    }

    private record LogMessage(String timestamp, LogType type, String message, String formattedLine) {

        public static LogMessage build(String inpLine) {
            String line = inpLine + "";
            String timestamp = null;
            LogType logType = null;

            if(line.matches("^\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] .+")) {
                timestamp = line.substring(0,line.indexOf("]")+1);
                line = line.replaceFirst("^\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] ","");

                if(line.matches("^\\[[^/\\]]+/[A-Z]+\\]:? .+")) {
                    logType = LogType.build(line.substring(line.indexOf("/")+1,line.indexOf("]")));
                    line = line.replaceFirst("^\\[[^/\\]]+/[A-Z]+\\]:? ","");
                }
            }        

            return build(timestamp, logType, line);
        }
        public static LogMessage build(LogType type, String message) {
            return build(FortytwoEdit.getTimestamp(), type, message);
        }
        public static LogMessage build(String timestamp, LogType type, String message) {
            StringBuilder formattedLine = new StringBuilder();

            if(timestamp != null)
                formattedLine.append(ss).append("9").append(timestamp).append(ss).append("r ");
            
            if(type != null)
                formattedLine.append(ss).append(type.formatCode()).append(type.text()).append(ss).append("r ");

            String formatMessage = message.replace("\t","  ");
            if(timestamp != null && type != null && formatMessage.matches("^\\([^)]+\\) .+")) {
                formattedLine.append(ss).append("3").append(formatMessage.substring(0,formatMessage.indexOf(")")+1)).append(ss).append("r ");
                formatMessage = formatMessage.replaceFirst("^\\([^)]+\\) ","");
            }
            formattedLine.append(formatMessage);

            return new LogMessage(timestamp, type, message, formattedLine.toString());
        }
    }

    public record LogType(String text, String formatCode) {
        public static final LogType INFO = new LogType("[INFO]","2");
        public static final LogType WARN = new LogType("[WARN]","6");
        public static final LogType ERROR = new LogType("[ERROR]","c");
        public static final LogType DEBUG = new LogType("[DEBUG]","a");
        public static final LogType FATAL = new LogType("[FATAL]","4");
        public static final LogType UNKNOWN = new LogType("[UNKNOWN]","f");

        public static LogType build(String text) {
            if(text != null && text.length()>0) {
                switch(text) {
                    case "INFO": return LogType.INFO;
                    case "WARN": return LogType.WARN;
                    case "ERROR": return LogType.ERROR;
                    case "DEBUG": return LogType.DEBUG;
                    case "FATAL": return LogType.FATAL;
                    default: return new LogType("[" + text + "]", LogType.UNKNOWN.formatCode());
                }
            }
            return LogType.UNKNOWN;
        }
    }

    public static void logModLog(LogType type, String message) {
        MOD_LOG_QUEUE.add(LogMessage.build(type,message));
    }

    public static void clearLogCache() {
        clearFullLogCache = true;
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

            if(!MOD_LOG_QUEUE.isEmpty()) {
                for(int i=0; i<MOD_LOG_QUEUE.size(); i++) {
                    MOD_LOG.add(MOD_LOG_QUEUE.get(i));
                }
                MOD_LOG_QUEUE.clear();
                if(onlyMod)
                    updateBox();
            }

            if(System.currentTimeMillis()-lastCheck >= UPDATE_WAIT_MS) {
                if(clearFullLogCache) {
                    lastCheck = 0;
                    lastUpdate = 0;
                    clearFullLogCache = false;
                    FULL_LOG.clear();
                }

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

                    if(!onlyMod)
                        updateBox();
                }
            }

        }

        super.tick();
    }

}
