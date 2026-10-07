"use client";

import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode
} from "react";
import { usePathname, useRouter } from "next/navigation";

import {
  getCurrentUser,
  logout as logoutRequest,
  refreshSession,
  type AuthUser
} from "@/lib/auth";

type AuthContextValue = {
  user: AuthUser;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthGate({ children }: { children: ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function resolveSession() {
      try {
        const currentUser = await getCurrentUser();
        if (!cancelled) {
          setUser(currentUser);
          setChecking(false);
        }
        return;
      } catch {
        // Access JWT may simply have expired; try refresh rotation once.
      }

      try {
        const refreshed = await refreshSession();
        if (!cancelled) {
          setUser(refreshed.user);
          setChecking(false);
        }
      } catch {
        if (!cancelled) {
          const next = encodeURIComponent(pathname || "/dashboard");
          router.replace(`/login?next=${next}`);
        }
      }
    }

    resolveSession();

    return () => {
      cancelled = true;
    };
  }, [pathname, router]);

  const value = useMemo<AuthContextValue | null>(() => {
    if (!user) {
      return null;
    }

    return {
      user,
      signOut: async () => {
        try {
          await logoutRequest();
        } finally {
          setUser(null);
          router.replace("/login");
          router.refresh();
        }
      }
    };
  }, [router, user]);

  if (checking || !value) {
    return (
      <main className="sessionLoading" aria-live="polite">
        <div className="sessionLoading__mark">A</div>
        <p>Checking your session…</p>
      </main>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthGate");
  }

  return context;
}
