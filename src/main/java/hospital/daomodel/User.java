package hospital.daomodel;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role; // "DOCTOR", "PATIENT" или "ADMIN"
    private int roleId;  // ID из соответствующей таблицы (DoctorID, PatientID или AdminID)

    // Геттеры и сеттеры
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        this.password = password.trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (!"DOCTOR".equals(role) && !"PATIENT".equals(role) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Недопустимая роль пользователя");
        }
        this.role = role;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        if (roleId < 0) {
            throw new IllegalArgumentException("ID роли должно быть неотрицательным числом");
        }
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role='%s', roleId=%d}",
                userId, username, role, roleId);
    }
}