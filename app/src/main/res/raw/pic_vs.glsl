#version 300 es
layout (location = 0) in vec4 a_Position;
layout (location = 1) in vec2 a_TextureCoord;
uniform mat4 u_MvpMatrix;
//输出纹理坐标(s,t)
out vec2 vTexCoord;
void main() {
    gl_Position  = u_MvpMatrix*a_Position;
    gl_PointSize = 10.0;
    vTexCoord = a_TextureCoord;
}