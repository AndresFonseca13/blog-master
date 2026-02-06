import instance from "./Api.ts";
import {
    type ApiResponse,
    type LoginRequest,
    type LoginResponse,
    type User,
    type UserRegisterRequest
} from "../types/Auth.ts";

export const login  = async  (credentials: LoginRequest) => {
    const response = await instance.post<ApiResponse<LoginResponse>>('/users/login', credentials);
    const token = response.data.data.token;

    if (token){
        localStorage.setItem('token', token);
    }
    return response;
}

export const register = async (credentials: UserRegisterRequest) => {
    return await instance.post<ApiResponse<User>>('/users/register', credentials);
}

export const logout = () => {
    localStorage.removeItem('token');
}

export const getMe = async () => {
    return await instance.get<ApiResponse<User>>('/users/me');
}