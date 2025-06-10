package io.github.some_example_name.entities;

public abstract class Entity {
    private int hp;
    private boolean defeated;
    private int damage;

    private String textureFileName;
    private int gridX; // Posição X do inimigo no grid do mapa
    private int gridY; // Posição Y do inimigo no grid do mapa
    private boolean facingRight;

    public Entity(int initialHp, String textureFileName, int initialGridX, int initialGridY, boolean facingRight) {
        this.hp = initialHp;
        this.defeated = false;
        this.gridX = initialGridX;
        this.gridY = initialGridY;
        this.textureFileName = textureFileName;
        this.facingRight = facingRight;
    }

    public int getHp() {
        return hp;
    }

    public void takeDamage(int amount) {
        this.hp -= amount;
        if (this.hp <= 0) {
            this.hp = 0;
            this.defeated = true;
        }
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }
}
