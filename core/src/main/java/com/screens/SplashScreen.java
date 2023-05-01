package com.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;

public class SplashScreen extends GameScreen
{

    private InputProcessor splashContinueProcessor;

    public SplashScreen(Game game)
    {
        super(game);
    }

    @Override
    public void show()
    {
        super.show();

        splashContinueProcessor = new InputAdapter()
        {
            public boolean keyDown (int keycode)
            {
                transitionTo(new MainMenuScreen(game));
                return false;
            }

            public boolean keyUp (int keycode)
            {
                transitionTo(new MainMenuScreen(game));
                return false;
            }

            public boolean keyTyped (char character)
            {
                transitionTo(new MainMenuScreen(game));
                return false;
            }

            public boolean touchDown (int screenX, int screenY, int pointer, int button)
            {
                transitionTo(new MainMenuScreen(game));
                return false;
            }

            public boolean touchUp (int screenX, int screenY, int pointer, int button)
            {
                transitionTo(new MainMenuScreen(game));
                return false;
            }
        };

        addInputProcessor(splashContinueProcessor);
    }

    @Override
    public void hide()
    {
        super.hide();
        removeInputProcessor(splashContinueProcessor);
    }

    @Override
    public void buildUI(Table table, Skin skin)
    {
        table.top();
        table.add(new Label("Dough or Die", skin, "splash_title"));
        table.row();

        Label continueLabel = new Label("Press any key to continue", skin, "splash_continue");
        Action continueAction = Actions.forever(Actions.sequence(Actions.fadeOut(1f), Actions.fadeIn(1f)));
        continueLabel.addAction(continueAction);
        table.add(continueLabel).padTop(400);
    }

    @Override
    public void update(float dt)
    {
    }

    @Override
    public void render()
    {
        ScreenUtils.clear(0.3f,0.3f,0.3f,1);
    }
}
