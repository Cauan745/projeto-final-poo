package io.github.some_example_name.entities;

/**
 * Representa o jogador no jogo, contendo atributos como HP, posição no grid
 * e direção visual.
 */
public class Player {
    private int hp;
    private int gridX;
    private int gridY;
    private boolean facingRight; // True se o jogador está virado para a direita

    /**
     * Construtor para criar uma nova instância do jogador.
     * @param initialHp HP inicial do jogador.
     * @param initialGridX Posição X inicial no grid do mapa.
     * @param initialGridY Posição Y inicial no grid do mapa.
     */
    public Player(int initialHp, int initialGridX, int initialGridY) {
        this.hp = initialHp;
        this.gridX = initialGridX;
        this.gridY = initialGridY;
        this.facingRight = true; // Começa olhando para a direita por padrão
    }

    /**
     * Obtém o HP atual do jogador.
     * @return HP atual.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Causa dano ao jogador, reduzindo seu HP. O HP não pode ser negativo.
     * @param amount A quantidade de dano a ser aplicada.
     */
    public void takeDamage(int amount) {
        this.hp -= amount;
        if (this.hp < 0) {
            this.hp = 0;
        }
    }

    /**
     * Verifica se o jogador está vivo (HP > 0).
     * @return True se o jogador está vivo, false caso contrário.
     */
    public boolean isAlive() {
        return this.hp > 0;
    }

    /**
     * Obtém a posição X do jogador no grid do mapa.
     * @return Coordenada X no grid.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * Define a posição X do jogador no grid do mapa.
     * @param gridX Nova coordenada X no grid.
     */
    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    /**
     * Obtém a posição Y do jogador no grid do mapa.
     * @return Coordenada Y no grid.
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * Define a posição Y do jogador no grid do mapa.
     * @param gridY Nova coordenada Y no grid.
     */
    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    /**
     * Verifica se o jogador está virado para a direita.
     * @return True se virado para a direita, false se virado para a esquerda.
     */
    public boolean isFacingRight() {
        return facingRight;
    }

    /**
     * Define a direção visual do jogador.
     * @param facingRight True para virar para a direita, false para a esquerda.
     */
    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }
}
