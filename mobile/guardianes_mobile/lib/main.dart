import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:guardianes_mobile/shared/theme/app_theme.dart';
import 'package:guardianes_mobile/core/utils/injection.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/presentation/pages/login_page.dart';
import 'package:guardianes_mobile/features/home/presentation/pages/home_page.dart';
import 'package:guardianes_mobile/features/walking/presentation/pages/step_tracking_page.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await configureDependencies();
  runApp(const GuardianesApp());
}

class GuardianesApp extends StatelessWidget {
  const GuardianesApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => getIt<AuthBloc>()..add(AuthCheckStatus()),
      child: MaterialApp(
        title: 'Guardianes de Gaia',
        theme: AppTheme.lightTheme,
        home: const AuthWrapper(),
        routes: {
          '/home': (context) => const HomePage(),
          '/login': (context) => const LoginPage(),
          '/step-tracking': (context) => BlocProvider(
            create: (context) => getIt<StepBloc>(),
            child: const StepTrackingPage(),
          ),
        },
      ),
    );
  }
}

class AuthWrapper extends StatelessWidget {
  const AuthWrapper({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AuthBloc, AuthState>(
      builder: (context, state) {
        if (state is AuthLoading) {
          return const Scaffold(
            body: Center(
              child: CircularProgressIndicator(),
            ),
          );
        } else if (state is AuthAuthenticated) {
          return const HomePage();
        } else {
          return const LoginPage();
        }
      },
    );
  }
}
