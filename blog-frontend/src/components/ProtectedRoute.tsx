import {useAuth} from "../hooks/useAuth.ts";
import {Navigate} from "react-router-dom";
import * as React from "react";

export const ProtectedRoute = ({ children} : {children: React.ReactNode}) => {
    const {isAuthenticated} = useAuth();

    return isAuthenticated ? children : <Navigate to="/login" replace />;
};