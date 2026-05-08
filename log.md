
----------------------------------------------------------------

# 42edit update guide

This file is to log important information related to maintaining the mod.

----------------------------------------------------------------

## UPDATE PROTOCOL

+ paste `gradle.properties` Fabric Properties and Dependencies and manually update Mod Properties
+ update `fabric.mod.json` depends minecraft version (for new full mc versions)
+ run commands in vscode terminal:
    + `./gradlew --stop`
    + `./gradlew genSources`
    + `./gradlew vscode`
+ edit `.vscode/launch.json`:
    + set args `"args": "--quickPlaySingleplayer",`
+ java project clean workspace

----------------------------------------------------------------

## BUILD

+ run in vscode terminal `./gradlew build`
+ jar will be in `build/libs/`

----------------------------------------------------------------

## UPDATE LAUNCHER

+ use fabric installer to get new launcher profile
+ copy 42jar from `build/libs/` to mods

----------------------------------------------------------------

## RARE UPDATES

+ delete folders:
    + `.gradle`
    + `.vscode`
    + `bin`
    + `build`
    + `run`
+ delete folders from windows user folder:
    + `.gradle`
    + `.cache`
+ run terminal commands:
    + `./gradlew clean`
    + `./gradlew --stop`
    + `./gradlew genSources`
    + `./gradlew vscode`
+ java project clean workspace
+ update example mod files from https://github.com/FabricMC/fabric-example-mod/
    + also see template https://fabricmc.net/develop/template/ (uncheck split client and common sources)
    + comment out fabric api from `build.gradle` dependencies
+ update FortytwoEdit.FEATURES
+ update SuggestionHelper and PathHelper (search HARDCODED)
+ verify ordinals (AbstractScrollAreaMixin/StringUtilMixin)

----------------------------------------------------------------

## TOOLS

+ Ctrl-P search MC classes starting with #
+ Mixin help https://fabricmc.net/wiki/tutorial:mixin_introduction

----------------------------------------------------------------

## CODE CLEANUP REGEX

+ general
    + files to exclude:
        + `.git, crash-reports, run/logs, gradlew, run/resourcepacks, run/saves, .gradle/, gradle.properties, .md`
    + regex search terms:
        + `^import.*\n\n+import`
        + `(if|while|for)\(`
        + `[a-zA-Z0-9]\{`
        + `(?<!\*)[ \t]+$`

+ java files
    + files to include:
        + `.java`
    + regex search terms:
        + `([^ \t](&&|\|\|))|((&&|\|\|)[^ \t])`
        + `([^ \t/*:]//)|([^:]//[^ \t/*])`
        + `([ \t][,;])`
        + `(,[^ \t])`
            + TODO
        + `(?<![\s+\-!=<>(])(([=](?![=]))|\+=|-=|\*=|/=|!=|==)|(([=](?![=]))|\+=|-=|\*=|/=|!=|==)(?![\s+\-!=<>);])`
            + TODO
        + `(?<![\s+\-!=<>(])([+](?![=+]))|([+](?![=+]))(?![\s+\-!=<>);])`
            + TODO
        + `([^ \t][%])|([%][^ \t])`
        + `([^ \t/*][*])|([*][^ \t/*])`
        + `([^ \t][ \t][ \t]+[^ \t])`
        + `([^ \t/*]((?<![<>:])[/]))|(((?<![<>:])[/])[^ \t/*])`
        + `(?<![\s+\-!=<>(])([\-](?![=\->]))|([\-](?![=\->]))(?![\s+\-!=<>);])`
            + TODO
        + match case on
            + `(?<![\s=<>]|Set|List|Map|String|Integer|Tag|Component|Optional|Identifier|T)(((?<![\-?/])[<>](?![?=]))|<=|>=|->|=>)|(((?<![\-?/])[<>](?![?=]))|<=|>=|->|=>)(?![\s=<>]|Set|List|Map|String|Integer|Tag|Component|Optional|Identifier|T)`

----------------------------------------------------------------
