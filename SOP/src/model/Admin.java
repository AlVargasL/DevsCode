package model;

/**
 * Modelo para la entidad Admin.
 */
public class Admin {
    private int id;
    private String correo;
    private String contrasena;

    /**
     * Constructor completo de Admin.
     * @param id Identificador único
     * @param correo Correo electrónico del administrador
     * @param contrasena Contraseña asociada
     */

    public Admin(int id, String correo, String contrasena) {
        this.id = id;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", correo='" + correo + "'}";
    }
}
