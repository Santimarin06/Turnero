import { Routes } from '@angular/router';
import { SystemLayoutComponent } from './layouts/system-layout/system-layout.component';
import { DashboardComponent } from './modules/dashboard/dashboard.component';
import { ClientesComponent } from './modules/clientes/clientes.component';
import { TurnosComponent } from './modules/turnos/turnos.component';
import { UsuariosComponent } from './modules/usuarios/usuarios.component';
import { LoginComponent } from './modules/login/login.component';
import { PublicComponent } from './modules/public/public.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
    {
        path: '', 
        component: PublicComponent
    },
    {
        path: 'login', 
        component: LoginComponent
    },
    {
        path: 'system', 
        component: SystemLayoutComponent, 
        canActivate: [authGuard],
        children: [
            {path: 'dashboard', component: DashboardComponent, canActivate: [authGuard]},
            {path: 'clientes', component: ClientesComponent, canActivate: [authGuard, roleGuard(['ADMIN'])]},
            {path: 'turnos', component: TurnosComponent, canActivate: [authGuard]},
            {path: 'usuarios', component: UsuariosComponent, canActivate: [authGuard, roleGuard(['ADMIN'])]},
            {path: '', redirectTo: 'dashboard', pathMatch: 'full'}
        ]
    },
    {path: '**', redirectTo: '', pathMatch: 'full'}
];
