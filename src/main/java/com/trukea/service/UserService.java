package com.trukea.service;

import com.trukea.dto.UserDTO;
import com.trukea.dto.RegisterRequestDTO;
import com.trukea.models.User;
import com.trukea.repository.UserRepository;

public class UserService {
    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserDTO authenticateUser(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user != null) {
            return convertToDTO(user);
        }
        return null;
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public int registerUser(RegisterRequestDTO request) {
        User user = new User();
        user.setNombre(request.getName());
        user.setApellido(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return userRepository.save(user);
    }

    public UserDTO getUserById(int id) {
        User user = userRepository.findById(id);
        if (user != null) {
            return convertToDTO(user);
        }
        return null;
    }

    public boolean updateUser(int id, User user) {
        return userRepository.update(id, user);
    }

    public void updateUserRating(int userId) {
        userRepository.updateRating(userId);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNombre(user.getNombre());
        dto.setApellido(user.getApellido());
        dto.setEmail(user.getEmail());
        dto.setCiudadNombre(user.getCiudadNombre());
        dto.setCalificacionPromedio(user.getCalificacionPromedio());
        dto.setImagenPerfil(user.getImagenPerfil());
        return dto;
    }
}