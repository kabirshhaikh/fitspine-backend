package com.fitspine.helper;

import com.fitspine.enums.*;

public class EnumScoreHelper {
    public static int pain(PainLevel painLevel) {
        if (painLevel == null) {
            return -1;
        }

        return switch (painLevel) {
            case NONE -> 0;
            case MILD -> 1;
            case MODERATE -> 2;
            case SEVERE -> 3;
            default -> -1;
        };
    }

    //Reverse mapping for pain:
    public static String painLabel(Integer value) {
        if (value == null || value < 0) {
            return null;
        }

        return switch (value) {
            case 0 -> "None";
            case 1 -> "Mild";
            case 2 -> "Moderate";
            case 3 -> "Severe";
            default -> null;
        };
    }

    public static int sittingTime(SittingTime sitting) {
        if (sitting == null) {
            return -1;
        }

        return switch (sitting) {
            case LESS_THAN_TWO_HOURS -> 0;
            case TWO_TO_FOUR_HOURS -> 1;
            case FOUR_TO_SIX_HOURS -> 2;
            case SIX_TO_EIGHT_HOURS -> 3;
            case GREATER_THAN_EIGHT_HOURS -> 4;
            default -> -1;
        };
    }

    public static int standingTime(StandingTime standing) {
        if (standing == null) {
            return -1;
        }

        return switch (standing) {
            case LESS_THAN_TWO_HOURS -> 0;
            case TWO_TO_FOUR_HOURS -> 1;
            case FOUR_TO_SIX_HOURS -> 2;
            case SIX_TO_EIGHT_HOURS -> 3;
            case GREATER_THAN_EIGHT_HOURS -> 4;
            default -> -1;
        };
    }

    public static int morningStiffness(MorningStiffness stiffness) {
        if (stiffness == null) {
            return -1;
        }

        return switch (stiffness) {
            case NONE -> 0;
            case MILD -> 1;
            case MODERATE -> 2;
            case SEVERE -> 3;
            default -> -1;
        };
    }

    //Reverse mapping for stiffness level:
    public static String morningStiffnessLabel(Integer value) {
        if (value == null || value < 0) {
            return null;
        }

        return switch (value) {
            case 0 -> "None";
            case 1 -> "Mild";
            case 2 -> "Moderate";
            case 3 -> "Severe";
            default -> null;
        };
    }

    public static int stressLevel(StressLevel stress) {
        if (stress == null) {
            return -1;
        }

        return switch (stress) {
            case VERY_LOW -> 0;
            case LOW -> 1;
            case MODERATE -> 2;
            case HIGH -> 3;
            case VERY_HIGH -> 4;
            default -> -1;
        };
    }

    public static int sleepDuration(SleepDuration sleepDuration) {
        if (sleepDuration == null) {
            return -1;
        }

        return switch (sleepDuration) {
            case LESS_THAN_5H -> 0;
            case H5_TO_6 -> 1;
            case H6_TO_7 -> 2;
            case H7_TO_8 -> 3;
            case MORE_THAN_8H -> 4;
            default -> -1;
        };
    }

    public static int nightWakeUps(NightWakeUps nightWakeUps) {
        if (nightWakeUps == null) {
            return -1;
        }

        return switch (nightWakeUps) {
            case NONE -> 0;
            case ONE -> 1;
            case TWO -> 2;
            case THREE_OR_MORE -> 3;
            default -> -1;
        };
    }

    public static String enumToStressLabel(Integer value) {
        if (value == null || value == -1) {
            return null;
        }

        return switch (value) {
            case 0 -> "Very Low";
            case 1 -> "Low";
            case 2 -> "Moderate";
            case 3 -> "High";
            case 4 -> "Very High";
            default -> null;
        };
    }

    public static String enumToTimeLabel(Integer value) {
        if (value == null || value == -1) {
            return null;
        }

        return switch (value) {
            case 0 -> "Less than 2h";
            case 1 -> "2–4 hours";
            case 2 -> "4–6 hours";
            case 3 -> "6–8 hours";
            case 4 -> "More than 8h";
            default -> null;
        };
    }
}
