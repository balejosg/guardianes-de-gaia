import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/home/presentation/pages/home_page.dart';

import 'home_page_test.mocks.dart';

@GenerateMocks([AuthBloc])
void main() {
  late MockAuthBloc mockAuthBloc;

  final testGuardian = Guardian(
    id: 1,
    username: 'test_guardian',
    email: 'test@example.com',
    name: 'Test Guardian',
    birthDate: DateTime.parse('2015-01-01'),
    age: 9,
    level: 'BEGINNER',
    experiencePoints: 150,
    experienceToNextLevel: 350,
    totalSteps: 5000,
    totalEnergyGenerated: 500,
    createdAt: DateTime.parse('2025-01-01'),
    lastActiveAt: DateTime.parse('2025-07-17'),
    isChild: true,
  );

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
        '/step-tracking': (context) =>
            const Scaffold(body: Text('Step Tracking Page')),
        '/profile': (context) => const Scaffold(body: Text('Profile Page')),
      },
    );
  }

  group('HomePage Widget Tests', () {
    testWidgets('should display guardian name and username',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: testGuardian));
      when(mockAuthBloc.stream)
          .thenAnswer((_) => Stream.value(AuthAuthenticated(guardian: testGuardian)));

      // act
      await tester.pumpWidget(makeTestableWidget(const HomePage()));

      // assert
      expect(find.text('Hola, Test Guardian'), findsOneWidget);
      expect(find.text('@test_guardian'), findsOneWidget);
      expect(find.text('Nivel: BEGINNER'), findsOneWidget);
    });

    testWidgets('should display XP and step statistics',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: testGuardian));
      when(mockAuthBloc.stream)
          .thenAnswer((_) => Stream.value(AuthAuthenticated(guardian: testGuardian)));

      // act
      await tester.pumpWidget(makeTestableWidget(const HomePage()));

      // assert
      expect(find.text('XP'), findsOneWidget);
      expect(find.text('150'), findsOneWidget);
      expect(find.text('Pasos Totales'), findsOneWidget);
      expect(find.text('5000'), findsOneWidget);
    });

    testWidgets('should display feature cards', (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: testGuardian));
      when(mockAuthBloc.stream)
          .thenAnswer((_) => Stream.value(AuthAuthenticated(guardian: testGuardian)));

      // act
      await tester.pumpWidget(makeTestableWidget(const HomePage()));

      // assert - check the first visible feature cards
      expect(find.text('Funciones Disponibles'), findsOneWidget);
      expect(find.text('Seguimiento de Pasos'), findsOneWidget);
      expect(find.text('Escanear Cartas'), findsOneWidget);
    });

    testWidgets('should navigate to step tracking when tapped',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: testGuardian));
      when(mockAuthBloc.stream)
          .thenAnswer((_) => Stream.value(AuthAuthenticated(guardian: testGuardian)));

      // act
      await tester.pumpWidget(makeTestableWidget(const HomePage()));
      await tester.tap(find.text('Seguimiento de Pasos'));
      await tester.pumpAndSettle();

      // assert
      expect(find.text('Step Tracking Page'), findsOneWidget);
    });

    testWidgets('should dispatch logout when logout button is tapped',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: testGuardian));
      when(mockAuthBloc.stream)
          .thenAnswer((_) => Stream.value(AuthAuthenticated(guardian: testGuardian)));

      // act
      await tester.pumpWidget(makeTestableWidget(const HomePage()));
      await tester.tap(find.byIcon(Icons.logout));
      await tester.pump();

      // assert
      verify(mockAuthBloc.add(argThat(isA<AuthLogoutRequested>()))).called(1);
    });

    testWidgets('should show loading indicator when not authenticated',
        (WidgetTester tester) async {
      // arrange
      when(mockAuthBloc.state).thenReturn(AuthInitial());
      when(mockAuthBloc.stream).thenAnswer((_) => Stream.value(AuthInitial()));

      // act
      await tester.pumpWidget(makeTestableWidget(const HomePage()));

      // assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });
  });
}
