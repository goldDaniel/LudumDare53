package com.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ArrayMap;

public class RenderResources
{
    private static boolean hasInitialized = false;

    private static SpriteBatch s;
    private static ShapeRenderer sh;

    private static ArrayMap<String, ShaderProgram> shaders;

    private static ArrayMap<String, Texture> textures;

    private static ArrayMap<String, BitmapFont> fonts;

    public static void init()
    {
        if(hasInitialized) throw new IllegalStateException("Render Resources already initialized");
        hasInitialized = true;

        s = new SpriteBatch();
        s.enableBlending();

        sh = new ShapeRenderer();


        textures = new ArrayMap<>();
        shaders = new ArrayMap<>();
        fonts = new ArrayMap<>();
    }

    public static Texture getTexture(String fileName)
    {
        if (textures.containsKey(fileName))
        {
            return textures.get(fileName);
        }

        FileHandle handle = Gdx.files.internal(fileName);
        if(handle.file().exists())
        {
            Texture t = new Texture(handle, true);
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            textures.put(fileName, t);
            return t;
        }
        else
        {
            return getTexture("textures/default.png");
        }
    }

    public static ShaderProgram getShader(String vertexPath, String fragmentPath)
    {
        String key = vertexPath + "##" + fragmentPath;
        if(shaders.containsKey(key))
        {
            return shaders.get(key);
        }

        FileHandle vertHandle = Gdx.files.internal(vertexPath);
        FileHandle fragHandle = Gdx.files.internal(fragmentPath);

        ShaderProgram shader = new ShaderProgram(vertHandle, fragHandle);
        if (!shader.isCompiled())
        {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        }

        return shader;
    }

    public static SpriteBatch getSpriteBatch()
    {
        return s;
    }

    public static ShapeRenderer getShapeRenderer()
    {
        return sh;
    }

    public static BitmapFont getFont(String fontName, int fontSize)
    {
        String key = fontName + "##" + fontSize;
        if(fonts.containsKey(key))
        {
            return fonts.get(key);
        }

        BitmapFont font = new BitmapFont(Gdx.files.internal("fonts/" + fontName + fontSize + ".fnt"));
        fonts.put(key, font);

        return font;
    }


    private static FileHandle getFontFileHandle(String filenameWithoutExtension)
    {
        String[] filetypes = {".otf", ".ttf"};

        for(String extension : filetypes)
        {
            String filename = filenameWithoutExtension + extension;
            FileHandle handle = Gdx.files.internal(filename);

            if(handle.exists())
            {
                return handle;
            }
        }

        throw new IllegalArgumentException("No font file found for font: " + filenameWithoutExtension);
    }
}
