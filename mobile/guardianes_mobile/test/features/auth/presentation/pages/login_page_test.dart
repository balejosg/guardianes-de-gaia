import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/presentation/pages/login_page.dart';

import 'login_page_test.mocks.dart';

@GenerateMocks([AuthBloc])
void main() {
  late MockAuthBloc mockAuthBloc;

  setUp(() {
    mockAuthBloc = MockAuthBloc();
  });

  Widget makeTestableWidget(Widget child) {
    return MaterialApp(
      home: BlocProvider<AuthBloc>.value(
        value: mockAuthBloc,
        child: child,
      ),
      routes: {
        '/home': (context) => const Scaffold(body: Text('Home Page')),
        '/register': (context) => const Scaffold(body: Text('Register Page')),
      },
    );
  }

  group('LoginPage Widget Tests', () {
    testWidgets('should display email and password fields',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      // assert
      expect(find.text('Usuario o Email'), findsOneWidget);
      expect(find.text('Contraseña'), findsOneWidget);
    });

    testWidgets('should show validation error for empty fields',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      // Find and tap the login button without filling fields
      final loginButton = find.widgetWithText(ElevatedButton, 'Iniciar Sesión');
      await tester.tap(loginButton);
      await tester.pump();

      // assert
      expect(find.text('Por favor ingresa tu usuario o email'), findsOneWidget);
    });

    testWidgets('should show SnackBar error message on AuthError state',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.fromIterable([
            AuthInitial(),
            const AuthError(message: 'Credenciales incorrectas'),
          ]));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));
      await tester.pumpAndSettle();

      // assert
      expect(find.text('Credenciales incorrectas'), findsOneWidget);
      expect(find.byType(SnackBar), findsOneWidget);
    });

    testWidgets('should show CircularProgressIndicator during loading',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthLoading());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthLoading()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      // assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should disable login button during loading',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthLoading());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthLoading()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      // assert
      final button =
          tester.widget<ElevatedButton>(find.byType(ElevatedButton).first);
      expect(button.onPressed, isNull);
    });

    testWidgets('should dispatch AuthLoginRequested when form is valid',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      await tester.enterText(
          find.widgetWithText(TextFormField, 'Usuario o Email'), 'testuser');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Contraseña'), 'password123');

      final loginButton = find.widgetWithText(ElevatedButton, 'Iniciar Sesión');
      await tester.tap(loginButton);
      await tester.pump();

      // assert
      verify(mockAuthBloc.add(argThat(isA<AuthLoginRequested>()))).called(1);
    });

    testWidgets('should have link to register page',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      // assert
      expect(find.text('¿No tienes cuenta? Regístrate'), findsOneWidget);
    });

    testWidgets('should toggle password visibility',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const LoginPage()));

      // Password visibility toggle should start with visibility icon
      expect(find.byIcon(Icons.visibility), findsOneWidget);

      // Toggle visibility
      await tester.tap(find.byIcon(Icons.visibility));
      await tester.pump();

      // Password should now show visibility_off icon
      expect(find.byIcon(Icons.visibility_off), findsOneWidget);
    });
  });
}
