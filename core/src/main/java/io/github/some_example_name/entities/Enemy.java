package io.github.some_example_name.entities;

/**
 * Representa um inimigo no jogo, contendo atributos como HP, status de derrotado
 * e posição no grid do mapa.
 */
public class Enemy extends Entity{

    public Enemy(int initialHp, String textureFileName, int initialGridX, int initialGridY, boolean facingRight) {
        super(initialHp, textureFileName, initialGridX, initialGridY, facingRight);
    }

}
