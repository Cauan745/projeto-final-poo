package io.github.some_example_name.entities;

/**
 * Representa um inimigo no jogo, contendo atributos como HP, status de derrotado
 * e posição no grid do mapa.
 */
public class Enemy {
    private int hp;
    private boolean defeated;
    private int gridX; // Posição X do inimigo no grid do mapa
    private int gridY; // Posição Y do inimigo no grid do mapa

    /**
     * Construtor para criar uma nova instância do inimigo.
     * @param initialHp HP inicial do inimigo.
     * @param initialGridX Posição X inicial no grid do mapa.
     * @param initialGridY Posição Y inicial no grid do mapa.
     */
    public Enemy(int initialHp, int initialGridX, int initialGridY) {
        this.hp = initialHp;
        this.defeated = false;
        this.gridX = initialGridX;
        this.gridY = initialGridY;
    }

    /**
     * Obtém o HP atual do inimigo.
     * @return HP atual.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Causa dano ao inimigo, reduzindo seu HP. O HP não pode ser negativo.
     * Define 'defeated' como true se o HP cair para 0 ou menos.
     * @param amount A quantidade de dano a ser aplicada.
     */
    public void takeDamage(int amount) {
        this.hp -= amount;
        if (this.hp <= 0) {
            this.hp = 0;
            this.defeated = true;
        }
    }

    /**
     * Verifica se o inimigo está derrotado (HP <= 0).
     * @return True se o inimigo está derrotado, false caso contrário.
     */
    public boolean isDefeated() {
        return defeated;
    }

    /**
     * Define o status de derrotado do inimigo.
     * @param defeated True para marcar como derrotado, false caso contrário.
     */
    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    /**
     * Obtém a posição X do inimigo no grid do mapa.
     * @return Coordenada X no grid.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * Obtém a posição Y do inimigo no grid do mapa.
     * @return Coordenada Y no grid.
     */
    public int getGridY() {
        return gridY;
    }
}
