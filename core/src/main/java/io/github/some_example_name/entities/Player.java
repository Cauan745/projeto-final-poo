package io.github.some_example_name.entities;

/**
 * Representa o jogador no jogo, contendo atributos como HP, posição no grid
 * e direção visual.
 */
public class Player extends Entity{

    public Player(int initialHp, String textureFileName, int initialGridX, int initialGridY, boolean facingRight) {
        super(initialHp, textureFileName, initialGridX, initialGridY, facingRight);
    }

}
