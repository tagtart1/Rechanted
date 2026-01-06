#version 150

uniform sampler2D MainDiffuse;

uniform vec3 ColorModulator;
uniform vec2 Resolution;
uniform float Time;

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

float inverseLerp(float v, float minValue, float maxValue) {
    return (v - minValue) / (maxValue - minValue);
}

float remap(float v, float inMin, float inMax, float outMin, float outMax) {
    float t = inverseLerp(v, inMin, inMax);
    return mix(outMin, outMax, t);
}

mat2 rotate2D(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c);
}

float stepped(float noiseSample) {
    float steppedSample = floor(noiseSample * 10.0) / 10.0;
    float remainder = fract(noiseSample * 10.0);
    steppedSample = (steppedSample - remainder) * 0.5 + 0.5;
    return steppedSample;
}

void main() {
    vec4 baseColor = texture(MainDiffuse, texCoord);

    // Center UVs so that (0, 0) is in the center instead of top right
    vec2 centerUV = texCoord - 0.5;

    // For scaling UVs such that the desired "grid" effect done below is always completely square
    // (which it can't be if UVs are stretched in any direction)
    float aspectR = Resolution.x / Resolution.y;
    //float aspectFacX = step(Resolution.y, Resolution.x); // If x is the larger axis, scale x by aspect ratio.
    //float aspectFacY = step(Resolution.x, Resolution.y);  // If y is the larger axis, scale y by aspect ratio.
    centerUV = vec2(centerUV.x, centerUV.y / aspectR);
    if (aspectR < 1.0) {
        centerUV = vec2(centerUV.x / aspectR, centerUV.y);
    }

    // Now perform directional offset over time (this is which way the grid will "scroll"
    vec2 timeOffsetCoord = (rotate2D(-120.0) * vec2((Time * 0.002), 0.0));
    timeOffsetCoord = centerUV - timeOffsetCoord;

    // Now rotate the UVs by an arbitrary amount so the grid is angled.
    vec2 rotatedCoords = (rotate2D(-30.0) * (timeOffsetCoord));

    // Finally scale up our coordinates (makes grid spaces "smaller"), and truncate all values the left of decimal point.
    // We do this so values are only between (0.0 - 1.0) but are still affected by previous effects. To generate the grid,
    // we simply undo the darkening if the coordinates are in the top right or bottom left quadrant
    // ...also "multFactor" just darkens the base diffuse color.
    rotatedCoords = fract(rotatedCoords * 10.0);
    float multFactor = 0.95;
    if ((rotatedCoords.x > 0.5 && rotatedCoords.y > 0.5) || (rotatedCoords.x < 0.5 && rotatedCoords.y < 0.5)) {
        multFactor = 1.0;
    }

    fragColor = vec4(baseColor.xyz * multFactor, baseColor.w);
}
