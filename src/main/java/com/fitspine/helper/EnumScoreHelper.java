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
}
