class Rng {

}

enum DiceType {
    D2, // Available, but also a "Coin"... See Coin class below
    D4,
    D6,
    D8,
    D10,
    D12,
    D20

    int faces

    DiceType() {
        this.faces = getFaces()
    }

    // Drops first char from type, and 'toInt()'s the remaining
    int getFaces(DiceType type) {
        String typeStr = type
        String numStr = typeStr.substring(1)
        faces = Integer.parseInt(numStr)
    }
}

class Dice {
    DiceType type
    int faces
    int roll

    Dice(DiceType type) {
        this.type = type
        this.faces = type.getFaces(type)
        this.roll = (Math.random() * faces) + 1
    }

    Dice roll() {
        return new Dice(type)
    }

    @Override
    String toString() {
        return this.roll
    }
}

class Coin {
    boolean val

    Coin() {
        this.val = Math.random() < 0.5
    }

    Coin flip() {
        return new Coin()
    }

    @Override
    String toString() {
        def out = this.val ? "Heads" : "Tails"
        return out
    }
}