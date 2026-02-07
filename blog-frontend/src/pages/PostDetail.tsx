import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getPostBySlug } from '../services/PostService.ts';
import type { Post } from '../types/Post.ts';

export const PostDetail = () => {
    const { slug } = useParams<{ slug: string }>();
    const navigate = useNavigate();
    const [post, setPost] = useState<Post | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!slug) {
            setError('Slug no proporcionado');
            setIsLoading(false);
            return;
        }

        const fetchPost = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const data = await getPostBySlug(slug);
                setPost(data);
            } catch (err) {
                console.log(err);
                setError('No se pudo cargar el post');
            } finally {
                setIsLoading(false);
            }
        };

        fetchPost();
    }, [slug]);

    if (isLoading) {
        return <p className="py-10 text-center text-gray-500">Cargando post...</p>;
    }

    if (error || !post) {
        return (
            <div className="py-10 text-center">
                <p className="mb-4 text-red-500">{error ?? 'Post no encontrado'}</p>
                <button
                    onClick={() => navigate('/')}
                    className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
                >
                    Volver al inicio
                </button>
            </div>
        );
    }

    const formattedDate = new Date(post.createdAt).toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    });

    return (
        <article className="mx-auto max-w-3xl px-4 py-8">
            <button
                onClick={() => navigate('/')}
                className="mb-6 text-sm text-blue-600 hover:underline"
            >
                &larr; Volver al inicio
            </button>

            {post.coverImage && (
                <img
                    src={post.coverImage}
                    alt={post.title}
                    className="mb-6 h-72 w-full rounded-lg object-cover"
                />
            )}

            <h1 className="mb-4 text-3xl font-bold text-gray-900">{post.title}</h1>

            <div className="mb-6 flex flex-wrap items-center gap-3">
                <time className="text-sm text-gray-500" dateTime={post.createdAt}>
                    {formattedDate}
                </time>

                {post.topics.length > 0 && (
                    <>
                        <span className="text-gray-300">|</span>
                        {post.topics.map((topic) => (
                            <span
                                key={topic}
                                className="rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800"
                            >
                                {topic}
                            </span>
                        ))}
                    </>
                )}
            </div>

            <div
                className="prose max-w-none"
                dangerouslySetInnerHTML={{ __html: post.content }}
            />
        </article>
    );
};