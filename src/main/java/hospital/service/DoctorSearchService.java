package hospital.service;

import hospital.DoctorDao;
import hospital.daomodel.Doctor;
import java.sql.SQLException;
import java.util.List;

public class DoctorSearchService {
    private final DoctorDao doctorDao = new DoctorDao();

    public List<Doctor> searchDoctors(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return doctorDao.getAllDoctors();
        }
        return doctorDao.searchDoctors(searchTerm.trim());
    }
}