import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'core/theme/app_theme.dart';
import 'core/router/app_router.dart';
import 'core/di/dependency_injection.dart';
import 'core/auth/auth_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  DependencyInjection.initialize();
  await AuthService().initialize();
  
  runApp(const GuardianesApp());
}

class GuardianesApp extends StatelessWidget {
  const GuardianesApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (context) => DependencyInjection.createStepTrackingBloc(),
        ),
      ],
      child: MaterialApp.router(
        title: 'Guardianes de Gaia',
        theme: AppTheme.theme,
        routerConfig: AppRouter.router,
        debugShowCheckedModeBanner: false,
      ),
    );
  }
}