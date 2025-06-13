package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.questions.QuestionData;

public class QuestionScreen implements Screen {
    final MyGdxGame game;
    private Stage stage;


    public QuestionScreen(final MyGdxGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        // A UI será construída no método show()
    }

    private void setupUI() {
        // Limpa a tela de atores antigos
        stage.clear();

        // Garante que temos uma pergunta válida
        if (game.currentQuestion == null) {
            Gdx.app.error("QuestionScreen", "currentQuestion é nula! Voltando para tela de combate.");
            game.setScreen(game.combatScreen);
            return;
        }

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;
        Label.LabelStyle labelStyle = new Label.LabelStyle(game.font, com.badlogic.gdx.graphics.Color.WHITE);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label questionLabel = new Label(game.currentQuestion.text, labelStyle);
        table.add(questionLabel).colspan(game.currentQuestion.options.length).pad(20).row();

        for (int i = 0; i < game.currentQuestion.options.length; i++) {
            final int optionIndex = i;
            TextButton optionButton = new TextButton(game.currentQuestion.options[i], textButtonStyle);
            optionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.lastAnswerCorrect = (optionIndex == game.currentQuestion.correctAnswerIndex);

                    // --- LÓGICA DO SRS ---
                    QuestionData qd = game.currentQuestionData;
                    long currentTime = System.currentTimeMillis();
                    long oneDayInMillis = 24 * 60 * 60 * 1000;

                    if (game.lastAnswerCorrect) {
                        // Se acertou: aumenta o intervalo e agenda a próxima revisão
                        qd.nextReviewTimestamp = currentTime + (qd.intervalDays * oneDayInMillis);
                        // Aumenta o intervalo para a próxima vez (ex: multiplicador de 1.8)
                        qd.intervalDays = (int) Math.ceil(qd.intervalDays * 1.8);
                        Gdx.app.log("SRS Update", "Acertou! Próxima revisão em " + qd.intervalDays + " dias.");

                        game.setReturnedFromQuestionScreenWithWrongAnswer(false);
                    } else {
                        // Se errou: reseta o intervalo e agenda para revisar logo
                        qd.intervalDays = 1; // Reseta o intervalo para 1 dia
                        // Agenda para 10 minutos a partir de agora, para ser revisada na mesma sessão
                        qd.nextReviewTimestamp = currentTime + (10 * 60 * 1000);
                        Gdx.app.log("SRS Update", "Errou! Próxima revisão em 10 minutos.");

                        game.setReturnedFromQuestionScreenWithWrongAnswer(true);
                    }
                    // --- FIM DA LÓGICA SRS ---

                    game.setScreen(game.combatScreen);
                }
            });
            table.add(optionButton).pad(10);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // Reconstrói a UI com a pergunta atual toda vez que a tela é mostrada
        setupUI();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    } // skin não é mais usada aqui
}
