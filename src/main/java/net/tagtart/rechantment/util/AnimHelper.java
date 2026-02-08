package net.tagtart.rechantment.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.Function;

import static net.minecraft.util.Mth.inverseLerp;

public class AnimHelper {

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

    public static class FloatKeyframe extends Keyframe<Float> {

        public FloatKeyframe(float startTime, Float value, Function<Float, Float> animationCurve) {
            super(startTime, value, animationCurve);
        }

        @Override
        public Float interpolateValue(Float a, Float b, float t) {
            return Mth.lerp(t, a, b);
        }
    }

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
        if (time < 0) {
            return keyframes.get(0).value;
        }
        else if (time >= keyframes.getLast().startTime) {
            return keyframes.getLast().value;
        }

        // Get first keyframe index with time value greater than one provided
        // We'll interpolate between this one and the next index
        int currKeyframe = 0;
        while (currKeyframe < keyframes.size() && time > keyframes.get(currKeyframe).startTime) {
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
        System.out.println(t);
        return k1.interpolateValue(k1.value, k2.value, t);
    }

    public static float linear(float t) {
        return t;
    }

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
        return t < 0.5 ? 2 * t * t : - 1 + ( 4 - 2 * t ) * t;
    }
}
