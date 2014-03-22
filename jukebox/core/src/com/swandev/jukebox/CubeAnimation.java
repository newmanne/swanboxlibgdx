package com.swandev.jukebox;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.primitives.MutableFloat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class CubeAnimation {

	// TODO: a lot of objects only make sense being in this class because this is the sole animation. If you ever had more, restructuring would make sense

	public Environment lights;
	public PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	Color modelColor = new Color();
	private final TweenManager tweenManager;
	MutableFloat angle = new MutableFloat(0);
	MutableFloat scale = new MutableFloat(1);
	private final Timeline timeline;

	public CubeAnimation() {
		tweenManager = new TweenManager();
		Tween.registerAccessor(Color.class, new ColorAccessor());
		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		lights.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10f, 10f, 10f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();

		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(modelColor)), Usage.Position | Usage.Normal);
		instance = new ModelInstance(model);
		// @formatter:off
		timeline = Timeline.createParallel()
				.beginSequence()
					.push(Tween.to(modelColor, ColorAccessor.R, 3f).target(1).ease(TweenEquations.easeNone).repeatYoyo(1, 0f).build())
					.push(Tween.to(modelColor, ColorAccessor.G, 3f).target(1).ease(TweenEquations.easeNone).repeatYoyo(1, 0f).build())
					.push(Tween.to(modelColor, ColorAccessor.B, 3f).target(1).ease(TweenEquations.easeNone).repeatYoyo(1, 0f).build())
				.end()
				.push(Tween.to(angle, 0, 3f).target(180).repeatYoyo(1, 0f).build())
				.push(Tween.to(scale, 0, 6f).target(2).repeatYoyo(1, 0f).build())
				.repeat(Tween.INFINITY, 0f).start(tweenManager);
		// @formatter:on
	}

	public void pause() {
		timeline.pause();
	}

	public void resume() {
		timeline.resume();
	}

	public void render() {
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		instance.materials.first().set(ColorAttribute.createDiffuse(modelColor));
		instance.transform.idt();
		instance.transform.rotate(new Vector3(0, 1, 0), angle.floatValue()).scale(scale.floatValue(), scale.floatValue(), scale.floatValue());

		modelBatch.begin(cam);
		modelBatch.render(instance, lights);
		modelBatch.end();
		tweenManager.update(Gdx.graphics.getDeltaTime());
	}

	public void dispose() {
		modelBatch.dispose();
		model.dispose();
	}

}
