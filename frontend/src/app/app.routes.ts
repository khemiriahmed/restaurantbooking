import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'restaurants',
        loadComponent: () =>
          import('./features/restaurants/restaurants.component').then(
            (m) => m.RestaurantsComponent,
          ),
      },
      {
        path: 'restaurants/:id',
        loadComponent: () =>
          import('./features/restaurant-detail/restaurant-detail.component').then(
            (m) => m.RestaurantDetailComponent,
          ),
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/register/register.component').then((m) => m.RegisterComponent),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
