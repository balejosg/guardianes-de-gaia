import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/presentation/pages/register_page.dart';

import 'register_page_test.mocks.dart';

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
      },
    );
  }

  group('RegisterPage Widget Tests', () {
    testWidgets('should display all form fields', (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      expect(find.text('Registrarse'), findsWidgets);
      expect(find.text('Nombre de Usuario'), findsOneWidget);
      expect(find.text('Email'), findsOneWidget);
      expect(find.text('Nombre Completo'), findsOneWidget);
      expect(find.text('Fecha de Nacimiento'), findsOneWidget);
      expect(find.text('Contraseña'), findsOneWidget);
      expect(find.text('Confirmar Contraseña'), findsOneWidget);
    });

    testWidgets('should show validation error for empty username',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // Find and tap the register button without filling fields
      final registerButton = find.widgetWithText(ElevatedButton, 'Registrarse');
      await tester.ensureVisible(registerButton);
      await tester.pumpAndSettle();
      await tester.tap(registerButton);
      await tester.pump();

      // assert
      expect(
          find.text('Por favor ingresa un nombre de usuario'), findsOneWidget);
    });

    testWidgets('should show validation error for invalid email',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // Enter username but invalid email
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Nombre de Usuario'), 'testuser');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Email'), 'invalid-email');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Nombre Completo'), 'Test User');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Contraseña'), 'password123');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Confirmar Contraseña'),
          'password123');

      final registerButton = find.widgetWithText(ElevatedButton, 'Registrarse');
      await tester.ensureVisible(registerButton);
      await tester.pumpAndSettle();
      await tester.tap(registerButton);
      await tester.pump();

      // assert
      expect(find.text('Ingresa un email válido'), findsOneWidget);
    });

    testWidgets('should show validation error for password mismatch',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      await tester.enterText(
          find.widgetWithText(TextFormField, 'Nombre de Usuario'), 'testuser');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Email'), 'test@example.com');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Nombre Completo'), 'Test User');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Contraseña'), 'password123');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Confirmar Contraseña'),
          'different123');

      final registerButton = find.widgetWithText(ElevatedButton, 'Registrarse');
      await tester.ensureVisible(registerButton);
      await tester.pumpAndSettle();
      await tester.tap(registerButton);
      await tester.pump();

      // assert
      expect(find.text('Las contraseñas no coinciden'), findsOneWidget);
    });

    testWidgets('should show SnackBar error message on AuthError state',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.fromIterable([
            AuthInitial(),
            const AuthError(message: 'El nombre de usuario ya está registrado'),
          ]));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));
      await tester.pumpAndSettle();

      // assert
      expect(
          find.text('El nombre de usuario ya está registrado'), findsOneWidget);
      expect(find.byType(SnackBar), findsOneWidget);
    });

    testWidgets('should show CircularProgressIndicator during loading',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthLoading());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthLoading()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should disable register button during loading',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthLoading());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthLoading()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      final button =
          tester.widget<ElevatedButton>(find.byType(ElevatedButton).first);
      expect(button.onPressed, isNull);
    });

    testWidgets('should show birth date picker when tapped',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));
      await tester.tap(find.text('Selecciona tu fecha de nacimiento'));
      await tester.pumpAndSettle();

      // assert
      expect(find.byType(DatePickerDialog), findsOneWidget);
    });

    testWidgets('should show error SnackBar when birth date is not selected',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // Fill all fields except birth date
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Nombre de Usuario'), 'testuser');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Email'), 'test@example.com');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Nombre Completo'), 'Test User');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Contraseña'), 'password123');
      await tester.enterText(
          find.widgetWithText(TextFormField, 'Confirmar Contraseña'),
          'password123');

      final registerButton = find.widgetWithText(ElevatedButton, 'Registrarse');
      await tester.ensureVisible(registerButton);
      await tester.pumpAndSettle();
      await tester.tap(registerButton);
      await tester.pump();

      // assert
      expect(find.text('Por favor selecciona tu fecha de nacimiento'),
          findsOneWidget);
    });

    testWidgets('should have link to login page', (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      expect(find.text('¿Ya tienes cuenta? Inicia sesión'), findsOneWidget);
    });
  });
}
