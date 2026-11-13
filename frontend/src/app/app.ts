import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Signalement, QuartierCount, SignalementService, Statut } from './signalement.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private service = inject(SignalementService);

  statuts: { valeur: Statut; libelle: string }[] = [
    { valeur: 'RECU', libelle: 'Reçu' },
    { valeur: 'EN_COURS', libelle: 'En cours' },
    { valeur: 'REPARE', libelle: 'Réparé' }
  ];

  quartier = '';
  description = '';
  fichier: File | null = null;
  erreur = '';
  signalements: Signalement[] = [];
  stats: QuartierCount[] = [];

  ngOnInit() { this.charger(); }

  charger() {
    this.service.lister().subscribe(l => this.signalements = l);
    this.service.stats().subscribe(s => this.stats = s);
  }

  choisir(event: Event) {
    const f = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.erreur = '';
    // Contrôle de confort : le vrai contrôle reste celui du serveur
    if (f && !['image/jpeg', 'image/png'].includes(f.type)) {
      this.erreur = 'Format refusé : JPEG ou PNG uniquement.';
      this.fichier = null; return;
    }
    if (f && f.size > 2 * 1024 * 1024) {
      this.erreur = 'Photo trop lourde (2 Mo maximum).';
      this.fichier = null; return;
    }
    this.fichier = f;
  }

  envoyer(input: HTMLInputElement) {
    if (!this.fichier) return;
    const fd = new FormData();
    fd.append('quartier', this.quartier);
    fd.append('description', this.description);
    fd.append('photo', this.fichier);

    this.service.creer(fd).subscribe({
      next: () => {
        this.quartier = ''; this.description = '';
        this.fichier = null; input.value = ''; this.erreur = '';
        this.charger();
      },
      error: err => {
        this.erreur =
          err.status === 413 ? 'Photo trop lourde (2 Mo maximum).' :
          err.status === 415 ? 'Format refusé : JPEG ou PNG uniquement.' :
          'Erreur lors de l’envoi du signalement.';
      }
    });
  }

  changer(s: Signalement, statut: Statut) {
    this.service.changerStatut(s.id, statut).subscribe(() => this.charger());
  }

  photo(nom: string) { return this.service.urlPhoto(nom); }
}