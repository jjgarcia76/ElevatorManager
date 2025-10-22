//Este es como un tipo de Enum, quizas se podria implementar en una clase aparte, enves de que sea una clase

package elevador.modelo;

public class Tipos{
    public enum Direccion {
        SUBE,
        BAJA,
        QUIETO
    }

    public enum TipoComando {
        IR_A
    }
}
