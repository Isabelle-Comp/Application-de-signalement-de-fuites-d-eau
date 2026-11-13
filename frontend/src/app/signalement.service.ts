import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type Statut = 'RECU' | 'EN_COURS' | 'REPARE';

export interface Signalement {
  id: number; quartier: string; description: string;
  photo: string; statut: Statut; dateCreation: string;
}
export interface QuartierCount { quartier: string; total: number; }

@Injectable({ providedIn: 'root' })
export class SignalementService {
  private http = inject(HttpClient);
  private base = '/api/signalements';

  lister()  { return this.http.get<Signalement[]>(this.base); }
  stats()   { return this.http.get<QuartierCount[]>(`${this.base}/stats`); }
  creer(fd: FormData) { return this.http.post<Signalement>(this.base, fd); }
  changerStatut(id: number, statut: Statut) {
    return this.http.patch<Signalement>(`${this.base}/${id}/statut`, null, { params: { statut } });
  }
  urlPhoto(nom: string) { return `${this.base}/photos/${nom}`; }
}