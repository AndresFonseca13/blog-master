import type {LoginRequest, LoginResponse, User} from "./Auth.ts";

export interface AuthContextType {
    user?: User
    isAuthenticated: boolean
    isLoading: boolean
    login: (credentials: LoginRequest) => Promise<LoginResponse>
    logout: () => void

}