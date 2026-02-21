import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { getUserById } from "../services/UserService";
import type { User } from "../types/Auth";
import { useAuth } from "../hooks/UseAuth";
import { useNavigate } from "react-router-dom";

export const ProfilePage = () => {
    const { id } = useParams<{ id: string }>();
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();
    const { user: currentUser } = useAuth();

    useEffect(() => {
        const fetchUser = async () => {
            setIsLoading(true);
            if (id) {
                try {
                    const data = await getUserById(id);
                    setUser(data);
                } catch (err) {
                    console.log(err);
                    setError("No se pudo cargar el usuario");
                } finally {
                    setIsLoading(false);
                }
            };
        };
        fetchUser();
    }, [id, currentUser?.id, navigate]);

    if (isLoading) {
        return <p className="py-10 text-center text-gray-500">Cargando usuario...</p>;
    }

    if (error || !user) {
        return <p className="py-10 text-center text-red-500">{error ?? "No se pudo cargar el usuario"}</p>;
    }

    const memberSince = new Date(user.createdAt).toLocaleDateString("es-ES", {
        year: "numeric",
        month: "long",
        day: "numeric",
    });

    const isOwnProfile = id === currentUser?.id;

    return (
        <div className="mx-auto max-w-3xl px-4 py-8">
            {/* Header del perfil */}
            <div className="mb-8 flex items-center gap-6">
                {user.profilePicture ? (
                    <img
                        src={user.profilePicture}
                        alt={user.username}
                        className="h-24 w-24 rounded-full object-cover"
                    />
                ) : (
                    <div className="flex h-24 w-24 items-center justify-center rounded-full bg-blue-100 text-3xl font-bold text-blue-600">
                        {user.username.charAt(0).toUpperCase()}
                    </div>
                )}

                <div className="flex-1">
                    <h1 className="text-2xl font-bold text-gray-900">{user.username}</h1>
                    <p className="mt-1 text-sm text-gray-500">Miembro desde {memberSince}</p>

                    {isOwnProfile && (
                        <button
                            onClick={() => navigate("/profile/edit")}
                            className="mt-3 rounded-md bg-blue-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-blue-700"
                        >
                            Editar perfil
                        </button>
                    )}
                </div>
            </div>

            {/* Posts del usuario */}
            <div>
                <h2 className="mb-4 text-xl font-semibold text-gray-800">Posts publicados</h2>
                <p className="text-sm text-gray-500">Próximamente...</p>
            </div>
        </div>
    );
};