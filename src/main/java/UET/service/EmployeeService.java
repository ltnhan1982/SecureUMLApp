package UET.service;

import UET.model.DTO.EmployeeDTO;
import UET.model.Employee;
import UET.model.Role;
import UET.model.User;
import UET.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserService userService;

    public void showEmployees(Model model, String roleName) {
        Role role = roleRepository.findByName(roleName);
        model.addAttribute("employees", this.getEmployeeInfo(role.getUsers()));
        model.addAttribute("departments", departmentRepository.findAll());
    }

    private List<EmployeeDTO> getEmployeeInfo(Set<User> users) {
        List<EmployeeDTO> employeeDTOs = new ArrayList<>();
        for (User user : users) {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(user.getEmployee().getId());
            employeeDTO.setFullName(user.getEmployee().getFullName());
            employeeDTO.setUserName(user.getUsername());
            if (!ObjectUtils.isEmpty(user.getEmployee().getDepartment()))
                employeeDTO.setDepartment(user.getEmployee().getDepartment().getType().name());
            employeeDTOs.add(employeeDTO);
        }
        return employeeDTOs;
    }

    public void createdEmployee(EmployeeDTO employeeDTO, String role, RedirectAttributes model) {
        if (!userService.checkUserNameExist(employeeDTO.getUserName())) {
            User user = new User();
            User userAuth = userService.getUserAth();
            Employee employee = new Employee();
            List<Role> roles = new ArrayList<>();
            user.setUsername(employeeDTO.getUserName());
            user.setPassword(passwordEncoder.encode("123456"));
            employee.setFullName(employeeDTO.getFullName());
            roles.add(roleRepository.findByName(role));
            if (departmentRepository.findById(employeeDTO.getIdDepartment()) != null && !role.equals("ROLE_RECEPTIONIST")) {
                employee.setDepartment(departmentRepository.findById(employeeDTO.getIdDepartment()));
            }
            user.setRoles(roles);
            employee.setUser(user);
            employeeRepository.save(employee);
            userRepository.save(user);
        } else {
            model.addFlashAttribute("errorUsername", true);
        }
    }
}
