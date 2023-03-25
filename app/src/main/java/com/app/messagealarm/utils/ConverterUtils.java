package com.app.messagealarm.utils;

public class ConverterUtils {

    public static long[]  genVibratorPattern( float intensity, long duration )
    {
        float dutyCycle = Math.abs( ( intensity * 2.0f ) - 1.0f );
        long hWidth = (long) ( dutyCycle * ( duration - 1 ) ) + 1;
        long lWidth = dutyCycle == 1.0f ? 0 : 1;
        int pulseCount = (int) ( 2.0f * ( (float) duration / (float) ( hWidth + lWidth ) ) );
        long[] pattern = new long[ pulseCount ];
        for( int i = 0; i < pulseCount; i++ )
        {
            pattern[i] = intensity < 0.5f ? ( i % 2 == 0 ? hWidth :
                    lWidth ) : ( i % 2 == 0 ? lWidth : hWidth );
        }
        return pattern;
    }

    public static long[] genVibratorPatternNew(float intensity, long duration) {
        // Calculate the number of pulses in the vibration pattern
        int pulseCount = (int) (duration / 50);
        // Calculate the duty cycle and pulse width based on the intensity
        float dutyCycle = intensity * 0.8f + 0.2f;
        long pulseWidth = (long) (duration * dutyCycle);
        // Create the vibration pattern
        long[] pattern = new long[pulseCount];
        for (int i = 0; i < pulseCount; i++) {
            // Calculate the intensity for this pulse
            float t = (float) i / (float) pulseCount;
            float pulseIntensity = 0.5f * (1.0f - (float) Math.cos(t * Math.PI));
            // Calculate the pulse duration based on the intensity
            long pulseDuration = (long) (pulseWidth * pulseIntensity);
            // Add the pulse to the pattern
            pattern[i] = pulseDuration;
            // Add a gap between pulses
            if (i < pulseCount - 1) {
                pattern[i] += (long) (duration * (1.0f - dutyCycle) / (pulseCount - 1));
            }
        }

        return pattern;
    }

}
