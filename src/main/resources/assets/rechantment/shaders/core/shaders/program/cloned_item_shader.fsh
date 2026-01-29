#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 Resolution;
uniform float Time;
uniform float FogStart;
uniform float FogEnd;
uniform float GlintAlpha;

in vec2 texCoord;
in float vertexDistance;

out vec4 fragColor;

const vec2 chromatic_aberation_uv_offset = vec2(0.0011, 0.001);
const vec3 clone_color_tint = vec3(0.757, 0.961, 0.95);

float inverseLerp(float v, float minValue, float maxValue) {
    return (v - minValue) / (maxValue - minValue);
}

// For transforming sin/cos derived values from [-1.0 to 1.0] range into an arbitrary one.
float remap(float v, float inMin, float inMax, float outMin, float outMax) {
    float t = inverseLerp(v, inMin, inMax);
    return mix(outMin, outMax, t);
}

void main() {

    // Oscillate chromatic abberation and tint multiplier over time
    float color_mult_offset = sin(Time * 0.14);
    float cloned_tint_mult = remap(color_mult_offset, -1.0, 1.0, 0.75, 1.0);
    float chromatic_aberation_mult = remap(color_mult_offset, -1.0, 1.0, 0.25, 0.4);

    // Sample texture of item, and tint by an arbitrary blue-ish color.
    vec4 timed_clone_tint = vec4((clone_color_tint * cloned_tint_mult).rgb, 1.0);
    vec4 color = texture(Sampler0, texCoord) * timed_clone_tint;
    if (color.a < 1.0) {
        discard;
    }

    // Quick chromatic abberation; not physically accurate at all just basic version and cool
    vec4 rSample = texture2D(Sampler0, texCoord - chromatic_aberation_uv_offset);
    vec4 gSample = texture2D(Sampler0, texCoord);
    vec4 bSample = texture2D(Sampler0, texCoord + chromatic_aberation_uv_offset);
    vec3 chroma_color;
    chroma_color.r = rSample.r;
    chroma_color.g = gSample.g;
    chroma_color.b = bSample.b;
    chroma_color *= chromatic_aberation_mult;
    color += vec4(chroma_color, 1.0);

    // Crt scan line effect; one layer of big, slow lines and another of small, faster lines
    // Essentially applies a darkening factor that oscillate depending on screen space fragment position,
    // offset by time so that they scroll slowly.
    float layer1Mult = sin((gl_FragCoord.y * 1.0) + Time * 0.3);
    float layer2Mult = sin((gl_FragCoord.y * 0.15) + Time * 0.1);

    layer1Mult = remap(layer1Mult, -1.0, 1.0, 0.77, 1.0);
    layer2Mult = remap(layer2Mult, -1.0, 1.0, 0.8, 1.0);
    color.rgb = color.rgb * layer1Mult * layer2Mult;

    fragColor = vec4(color.rgb, 0.7);
}
