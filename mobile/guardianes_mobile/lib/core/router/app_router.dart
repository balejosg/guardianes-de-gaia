import 'package:go_router/go_router.dart';
import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/step_tracking/presentation/pages/step_tracking_page.dart';
import '../auth/auth_service.dart';

class AppRouter {
  static final GoRouter router = GoRouter(
    initialLocation: '/',
    redirect: (context, state) async {
      final authService = AuthService();
      final isAuthenticated = await authService.isAuthenticated();
      final currentLocation = state.matchedLocation;
      
      if (!isAuthenticated && currentLocation != '/login') {
        return '/login';
      }
      
      if (isAuthenticated && currentLocation == '/login') {
        return '/';
      }
      
      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginPage(),
      ),
      GoRoute(
        path: '/',
        name: 'home',
        builder: (context, state) => const StepTrackingPage(),
      ),
    ],
  );
}