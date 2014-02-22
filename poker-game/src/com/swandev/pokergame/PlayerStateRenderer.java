package com.swandev.pokergame;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.google.common.collect.Maps;

public class PlayerStateRenderer {
	
	private static final float CAMERA_WIDTH = 9f;
	private static final float CAMERA_HEIGHT = 8f;
	
	private static final float CARD_WIDTH = 3f;
	private static final float CARD_HEIGHT = 4f;
	
	private static final float CARD1_ORIGIN_X = 1f;
	private static final float CARD1_ORIGIN_Y = 3f;
	private static final float CARD2_ORIGIN_X = 5f;
	private static final float CARD2_ORIGIN_Y = 3f;
	
	private PlayerState state;
	private OrthographicCamera cam;
	
	/** Textures **/
	private Map<Integer, TextureRegion> cardTextureMap = Maps.newHashMap();
	
	/** Animations **/
	//private Animation rollLeftAnimation;
	//private Animation rollRightAnimation;
	
	private final Stage stage;
	private SpriteBatch spriteBatch;
	private int width;
	private int height;
	private float ppuX; // pixels per unit on the X axis
	private float ppuY; // pixels per unit on the Y axis
	
	public void setSize(int w, int h){
		this.width = w;
		this.height = h;
		ppuX = (float)width / CAMERA_WIDTH;
		ppuY = (float)height / CAMERA_HEIGHT;
	}
	
	public PlayerStateRenderer(PlayerState state){
		this.state = state;
		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
		this.cam.update();
		this.spriteBatch = new SpriteBatch();
		
		//Stage fills up underneath the cards
		this.stage = new Stage(CAMERA_WIDTH*ppuX, Math.min(CARD1_ORIGIN_Y, CARD2_ORIGIN_Y)*ppuY);
		
		loadTextures();
	}
	
	private void loadTextures(){
		this.cardTextureMap = PokerLib.getCardTextures();
		
		TextureAtlas buttonAtlas = new TextureAtlas(Gdx.files.internal("images/buttons/textures/ButtonImages.pack"));
		
		TextureRegion cycleNumTexture = buttonAtlas.findRegion("next-card");
		this.state.nextCardButton = new Button(new TextureRegionDrawable(cycleNumTexture));
		
		TextureRegion cycleSuitTexture = buttonAtlas.findRegion("next-suit");
		this.state.nextSuitButton = new Button(new TextureRegionDrawable(cycleSuitTexture));
		
		buildStage();
	}
	
	public void render(float delta){
		spriteBatch.begin();
			drawCards();
		spriteBatch.end();
		stage.draw();
		stage.act(delta);
	}
	
	private void drawCards(){
		spriteBatch.draw(this.cardTextureMap.get(this.state.card1), CARD1_ORIGIN_X * ppuX, CARD1_ORIGIN_Y * ppuY, CARD_WIDTH * ppuX, CARD_HEIGHT * ppuY);
		spriteBatch.draw(this.cardTextureMap.get(this.state.card2), CARD2_ORIGIN_X * ppuX, CARD2_ORIGIN_Y * ppuY, CARD_WIDTH * ppuX, CARD_HEIGHT * ppuY);
	}
	
	private void buildStage(){
		Table table = new Table();
		table.add(this.state.nextCardButton);
		table.add(this.state.nextSuitButton);
		this.stage.addActor(table);
	}
}
