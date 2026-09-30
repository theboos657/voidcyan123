#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

// Previous (already blended) frame, drawn over the new frame with the blend weight in ColorModulator.a.
void main() {
    fragColor = vec4(texture(InSampler, texCoord).rgb, ColorModulator.a);
}
