import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/presentation/pages/register_page.dart';

// Use MockBloc from bloc_test for proper BLoC mocking
class MockAuthBloc extends MockBloc<AuthEvent, AuthState> implements AuthBloc {}

void main() {
  late MockAuthBloc mockAuthBloc;

  setUp(() {
    mockAuthBloc = MockAuthBloc();
  });

  tearDown(() {
    mockAuthBloc.close();
  });

  Widget makeTestableWidget(Widget child) {
    return MaterialApp(
      home: BlocProvider<AuthBloc>.value(
        value: mockAuthBloc,
        child: child,
      ),
      routes: {
        '/home': (context) => const Scaffold(body: Text('Home Page')),
        '/login': (context) => const Scaffold(body: Text('Login Page')),
      },
    );
  }

  group('RegisterPage Widget Tests', () {
    testWidgets('should display all form fields', (WidgetTester tester) async {
      // arrange
      when(() => mockAuthBloc.state).thenReturn(AuthInitial());
      whenListen(mockAuthBloc, Stream<AuthState>.fromIterable([AuthInitial()]));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      expect(find.text('Crear Cuenta'), findsWidgets);
    });

    testWidgets('should show CircularProgressIndicator during loading',
        (WidgetTester tester) async {
      // arrange
      when(() => mockAuthBloc.state).thenReturn(AuthLoading());
      whenListen(mockAuthBloc, Stream<AuthState>.fromIterable([AuthLoading()]));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      expect(find.byType(CircularProgressIndicator), findsWidgets);
    });

    testWidgets('should have link to login page', (WidgetTester tester) async {
      // arrange
      when(() => mockAuthBloc.state).thenReturn(AuthInitial());
      whenListen(mockAuthBloc, Stream<AuthState>.fromIterable([AuthInitial()]));

      // act
      await tester.pumpWidget(makeTestableWidget(const RegisterPage()));

      // assert
      expect(find.textContaining('Inicia sesión'), findsWidgets);
    });
  });
}
