package net.tagtart.rechantment.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.Function;

import static net.minecraft.util.Mth.inverseLerp;

public class AnimHelper {

    // Below is a very basic keyframe class and helper methods. Keyframes can be defined in terms of any time unit (ticks, seconds, etc.)
    // it's just up to the user as to how they are defined relative to each other in a sequence, and how often the keyframes are evaluated.
    //
    // Keyframe animation calculations will grab the two keyframes that the given time value sits between, and then
    // return an interpolated value between them that is affected by the first keyframes animation curve function.
    // The interpolateValue function just determines how the stored type will be affected by the time t.
    //
    // For example, a basic float keyframe just returns Mth.lerp(keyframe1.value, keyframe2.value, t)
    //
    // There are also animation curve methods at the bottom; they just affect the provided t-value with
    // easing functions that are common in animation programs so that it changes in interesting ways over time.
    // Pass a reference to one of these methods into a keyframe's animationCurve field, and it will apply the
    // easing function when that keyframe is active (i.e. time provided is greater that its startTime but less than
    // the next keyframes startTime)
    public static abstract class Keyframe<T> {

        public float startTime;
        public T value;
        public Function<Float, Float> animationCurve;

        public Keyframe(float startTime, T value, Function<Float, Float> animationCurve) {
            this.startTime = startTime;
            this.value = value;
            this.animationCurve = animationCurve;
        }

        abstract protected T interpolateValue(T a, T b, float t);
    }

    // Basic float type;
    public static class FloatKeyframe extends Keyframe<Float> {

        public FloatKeyframe(float startTime, Float value, Function<Float, Float> animationCurve) {
            super(startTime, value, animationCurve);
        }

        @Override
        public Float interpolateValue(Float a, Float b, float t) {
            return Mth.lerp(t, a, b);
        }
    }

    // Component-wise interpolation between vec3s
    public class Vec3Keyframe extends Keyframe<Vec3> {

        public Vec3Keyframe(float startTime, Vec3 value, Function<Float, Float> animationCurve) {
            super(startTime, value, animationCurve);
        }

        @Override
        protected Vec3 interpolateValue(Vec3 a, Vec3 b, float t) {

            // Hate new-ing a Vec3 on each call like this but component members are final... wtf.
            return new Vec3(
                    Mth.lerp(t, a.x, b.x),
                    Mth.lerp(t, a.y, b.y),
                    Mth.lerp(t, a.z, b.z)
            );
        }
    }

    public static <V> V evaluateKeyframes(ArrayList<? extends Keyframe<V>> keyframes, float time) {

        // Can't interpolate past first and last keyframes so just return their values directly.
        if (time <= keyframes.getFirst().startTime) {
            return keyframes.get(0).value;
        }
        else if (time >= keyframes.getLast().startTime) {
            return keyframes.getLast().value;
        }

        // Get first keyframe index with time value greater than one provided
        // We'll interpolate between this one and the next index
        int currKeyframe = 0;
        while (currKeyframe < keyframes.size() && time >= keyframes.get(currKeyframe).startTime - 0.001f) {
            currKeyframe++;
        }
        currKeyframe--;

        Keyframe<V> k1 = keyframes.get(currKeyframe);
        Keyframe<V> k2 = keyframes.get(currKeyframe + 1);

        // Get t value [0.0 - 1.0] of current time between two current keyframes and distort with animation curve.
        // Animation curve methods expect a t in that range.
        // Using k1's animation curve does mean that, yes, the last keyframe in the keyframe array will always be ignored!
        float at = k1.startTime;
        float bt = k2.startTime;
        float t = inverseLerp(time, at, bt);
        t = k1.animationCurve.apply(t);

        return k1.interpolateValue(k1.value, k2.value, t);
    }

    public static float linear(float t) {
        return t;
    }

    // EASING FUNCTIONS

    public static float easeOutCirc(float t) {
        double t1 = t - 1.0;
        return (float)Math.sqrt(1.0 - t1 * t);
    }

    public static float easeOutBack(float t) {
        float magnitude = 1.70158f;
        float scaledTime = t - 1;

        return (scaledTime * scaledTime * ((magnitude + 1) * scaledTime + magnitude)) + 1;
    }

    public static float easeInBack(float t) {
        float magnitude = 1.70158f;
        return t * t * ((magnitude + 1) * t - magnitude);
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5 ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2.0f;
    }

    public static float easeInOutBack(float t) {

        float magnitude = 1.70158f;
        float scaledTime = t * 2;
        float scaledTime2 = scaledTime - 2;

        float s = magnitude * 1.525f;

        if (scaledTime < 1) {
            return 0.5f * scaledTime * scaledTime * (
                    ((s + 1f) * scaledTime) - s
            );
        }

        return 0.5f * (
            scaledTime2 * scaledTime2 * ((s + 1f) * scaledTime2 + s) + 2f
        );
    }
}
