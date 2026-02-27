#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelMat;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;
uniform float Time;

out vec2 texCoord;
out vec4 vertexColor;

void main() {

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
    vertexColor = Color;
}