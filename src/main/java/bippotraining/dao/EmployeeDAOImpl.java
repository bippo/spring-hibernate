package bippotraining.dao;

import bippotraining.model.Employee;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeDAOImpl implements EmployeeDAO {

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public void addEmployee(Employee emp) {
        sessionFactory.getCurrentSession().save(emp);
    }

    @Override
    public void removeEmployee(String employeeId) {
        sessionFactory.getCurrentSession().remove(sessionFactory.getCurrentSession().byId(Employee.class).getReference(employeeId));
    }

    @Override
    public void updateEemployee(Employee emp) {
        sessionFactory.getCurrentSession().update(emp);
    }

    @Override
    public List<Employee> getEmployee() {
        return sessionFactory.getCurrentSession().createQuery("from Employee", Employee.class).list();
    }
}
