package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class ExtendedShader extends Shader {
	ProgramUniforms uniforms;
	GlFramebuffer writingTo;
	GlFramebuffer baseline;

	public ExtendedShader(ResourceFactory resourceFactory, String string, VertexFormat vertexFormat, GlFramebuffer writingTo, GlFramebuffer baseline, Consumer<LocationalUniformHolder> uniformCreator) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getProgramRef();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		uniformCreator.accept(uniformBuilder);

		uniforms = uniformBuilder.buildUniforms();
		this.writingTo = writingTo;
		this.baseline = baseline;
	}

	// TODO: Yarn WTF: That's the unbind method, not the bind method!
	@Override
	public void bind() {
		super.bind();

		baseline.bind();
	}

	// TODO: Yarn WTF: That's the bind method...
	@Override
	public void upload() {
		super.upload();

		uniforms.update();
		writingTo.bind();
	}

	public void addIrisSampler(String name, int id) {
		super.addSampler(name, id);
	}

	@Override
	public void addSampler(String name, Object sampler) {
		// Translate vanilla sampler names to Iris / ShadersMod sampler names
		if (name.equals("Sampler0")) {
			name = "texture";

			// "tex" is also a valid sampler name.
			super.addSampler("tex", sampler);
		} else if (name.equals("Sampler2")) {
			name = "lightmap";
		} else if (name.startsWith("Sampler")) {
			// We only care about the texture and the lightmap for now from vanilla.
			// All other samplers will be coming from Iris.
			return;
		} else {
			Iris.logger.warn("Iris: didn't recognize the sampler name " + name + " in addSampler, please use addIrisSampler for custom Iris-specific samplers instead.");
			return;
		}

		// TODO: Expose Sampler1 (the mob overlay flash)

		super.addSampler(name, sampler);
	}
}