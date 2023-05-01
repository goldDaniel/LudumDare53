package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.core.AudioResources;
import com.core.MusicMaster;
import com.core.RenderResources;

import java.util.ArrayList;

public class CutsceneScreen extends GameScreen
{
    public enum CutsceneType
    {
        INTRO,
        LOSE,
        WIN
    }

    private int[] currIndices;

    private final ArrayList<Image> images = new ArrayList<>();
    private final CutsceneType type;

    public CutsceneScreen(Game game, CutsceneType type)
    {
        super(game);
        this.type = type;

        String path = "textures\\cutscenes\\";
        int numPictures = 0;
        switch(type)
        {
            case INTRO:
            {
                currIndices = new int[]{0, 1, 0, 2, 3, 4, 5, 6, 7, 8};
                numPictures = 9;
                path += "intro\\intro_";
                break;
            }
            case LOSE:
            {
                currIndices = new int[]{0, 4, 2, 5};
                numPictures = 6;
                path += "outro\\outro_";
                break;
            }
            case WIN:
            {
                currIndices = new int[]{0, 1, 2, 3, 0, 4, 2, 5};
                numPictures = 6;
                path += "outro\\outro_";
                break;
            }
            default : break;
        }

        for (int i = 0; i < numPictures; i++)
        {
            Image image = new Image(new Texture(path + i + ".png"));
            image.setWidth(viewport.getWorldWidth());
            image.setHeight(viewport.getWorldHeight());
            images.add(image);
        }

        if(type == CutsceneType.INTRO)
        {
            MusicMaster.playMusic("intro", true);
        }
        else if(type == CutsceneType.LOSE)
        {
            MusicMaster.playMusic("outro_loop", true);
        }
        else
        {
            MusicMaster.playSequentialMusic(true, "outro_start", "outro_loop");
        }
    }

    @Override
    public void buildUI(Table table, Skin skin)
    {

    }

    private static final float IMAGE_DURATION = 2;
    private float imageTimer = IMAGE_DURATION;
    private int currImageIndex = 0;
    @Override
    public void update(float dt)
    {
        imageTimer -= dt;
        if(imageTimer < 0)
        {
            currImageIndex++;
            imageTimer = IMAGE_DURATION;
        }
    }

    @Override
    public void render()
    {
        if(currImageIndex >= currIndices.length)
        {
            switch(type)
            {
                case INTRO: game.setScreen(new GameplayScreen(game)); break;
                case LOSE:
                case WIN: game.setScreen(new MainMenuScreen(game)); break;
            }
        }
        else
        {
            RenderResources.getSpriteBatch().begin();
            images.get(currIndices[currImageIndex]).draw(RenderResources.getSpriteBatch(), 1);
            RenderResources.getSpriteBatch().end();
        }
    }
}
