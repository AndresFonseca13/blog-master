import { createContext, useState, useEffect } from "react";
import type {AuthContextType} from "../types/AuthContextType.ts";
import * as React from "react";
import type {LoginRequest, User} from "../types/Auth.ts";
import {login, logout, getMe} from "../services/AuthService.ts";

/* eslint-disable react-refresh/only-export-components */
export const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [ user, setUser ] = useState<User | undefined>(undefined);
    const [isLoading, setIsLoading] = useState<boolean>(true);

    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                setIsLoading(false);
                return;
            }

            try {
                const response = await getMe();
                setUser(response.data.data);
            } catch {
                localStorage.removeItem('token');
            } finally {
                setIsLoading(false);
            }
        };

        checkAuth();
    }, []);

    const handleLogin  = async (credentials: LoginRequest) => {
        const response = await login(credentials);
        setUser(response.data.data.user);
        return response.data.data;
    }

    const handleLogout = () => {
        logout();
        setUser(undefined);
    }

    return(
        <AuthContext.Provider value={ { user, isAuthenticated: !!(user), isLoading, login: handleLogin, logout: handleLogout  } }>
            {children}
        </AuthContext.Provider>
    )
}

