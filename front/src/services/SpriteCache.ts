class SpriteCache {
    private blobCache = new Map<string, string>();
    private loadingPromises = new Map<string, Promise<string>>();

    /**
     * Gère l'accès au cache et s'assure qu'une seule requête est lancée par chemin
     * @param key Le chemin unique du sprite
     * @param fetcher La fonction qui récupère le blob si non présent en cache
     */
    async getOrFetch(
        key: string,
        fetcher: () => Promise<Blob>
    ): Promise<string> {
        // 1. Retour du cache si existant
        if (this.blobCache.has(key)) {
            return this.blobCache.get(key)!;
        }

        // 2. Attente si une requête est déjà en cours
        if (this.loadingPromises.has(key)) {
            return this.loadingPromises.get(key)!;
        }

        // 3. Exécution de la requête
        const loadPromise = (async () => {
            try {
                const blob = await fetcher();
                const blobUrl = URL.createObjectURL(blob);
                this.blobCache.set(key, blobUrl);
                return blobUrl;
            } catch (error) {
                return "";
            } finally {
                this.loadingPromises.delete(key);
            }
        })();

        this.loadingPromises.set(key, loadPromise);
        return loadPromise;
    }

    /**
     * Libère la mémoire
     */
    clear() {
        this.blobCache.forEach((url) => URL.revokeObjectURL(url));
        this.blobCache.clear();
        this.loadingPromises.clear();
    }

    delete(key: string): boolean {
        const blobUrl = this.blobCache.get(key);

        if (blobUrl) {
            // 1. Libère la ressource mémoire dans le navigateur
            URL.revokeObjectURL(blobUrl);

            // 2. Supprime l'entrée du cache
            return this.blobCache.delete(key);
        }

        return false;
    }

    deleteByName(name: string): void {
        // 1. Identifier les clés à supprimer sans modifier la Map immédiatement
        const keysToDelete: string[] = [];

        this.blobCache.forEach((_, key) => {
            if (key.includes(name)) {
                keysToDelete.push(key);
            }
        });

        // 2. Procéder à la suppression réelle
        keysToDelete.forEach((key) => {
            const blobUrl = this.blobCache.get(key);
            if (blobUrl) {
                URL.revokeObjectURL(blobUrl);
                this.blobCache.delete(key);
            }
        });
    }
}

export const spriteCache = new SpriteCache();
