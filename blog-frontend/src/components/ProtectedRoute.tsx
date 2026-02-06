import {useAuth} from "../hooks/UseAuth"
import {Navigate} from "react-router-dom";
import * as React from "react";

export const ProtectedRoute = ({ children} : {children: React.ReactNode}) => {
    const {isAuthenticated, isLoading} = useAuth();

    if (isLoading) {
        return <div className="min-h-screen flex items-center justify-center">Cargando...</div>;
    }

    return isAuthenticated ? children : <Navigate to="/login" replace />;
};