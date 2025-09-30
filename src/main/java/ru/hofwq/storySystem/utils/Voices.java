package ru.hofwq.storySystem.utils;

public enum Voices {
    STORY_1("minecraft:custom.story1"),
    STORY_2("minecraft:custom.story2"),
    STORY_3("minecraft:custom.story3"),
    STORY_4("minecraft:custom.story4"),
    STORY_5("minecraft:custom.story5"),
    STORY_6("minecraft:custom.story6"),
    STORY_7("minecraft:custom.story7"),
    STORY_8("minecraft:custom.story8"),
    STORY_9("minecraft:custom.story9"),
    STORY_10("minecraft:custom.story10"),
    STORY_11("minecraft:custom.story11"),
    STORY_12("minecraft:custom.story12"),
    STORY_13("minecraft:custom.story13"),
    STORY_14("minecraft:custom.story14"),
    STORY_15("minecraft:custom.story15"),
    STORY_16("minecraft:custom.story16"),
    STORY_17("minecraft:custom.story17"),
    STORY_18("minecraft:custom.story18"),
    STORY_19("minecraft:custom.story19"),
    STORY_20("minecraft:custom.story20"),
    STORY_21("minecraft:custom.story21"),
    STORY_22("minecraft:custom.story22"),
    STORY_23("minecraft:custom.story23"),
    STORY_24("minecraft:custom.story24"),
    STORY_25("minecraft:custom.story25"),
    STORY_26("minecraft:custom.story26"),
    STORY_27("minecraft:custom.story27"),
    STORY_28("minecraft:custom.story28"),
    STORY_29("minecraft:custom.story29"),
    STORY_30("minecraft:custom.story30"),
    STORY_31("minecraft:custom.story31");

    private final String sound;

    Voices(String sound) {
        this.sound = sound;
    }

    public static String getSoundById(int id) {
        if (id < 1 || id > values().length) {
            return null;
        }

        return values()[id - 1].sound;
    }
}
