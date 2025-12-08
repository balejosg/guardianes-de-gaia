import 'package:bloc_test/bloc_test.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_bloc.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_event.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_state.dart';
import 'package:guardianes_mobile/features/home/presentation/pages/home_page.dart';
import 'package:mocktail/mocktail.dart';

// Mocks
class MockAuthBloc extends MockBloc<AuthEvent, AuthState> implements AuthBloc {}
class MockCardBloc extends MockBloc<CardEvent, CardState> implements CardBloc {}

// Fake States if needed (usually just use the real classes if they are simple data classes)

void main() {
  late MockAuthBloc mockAuthBloc;
  late MockCardBloc mockCardBloc;

  final tGuardian = Guardian(
    id: 1,
    username: 'test_guardian',
    email: 'test@test.com',
    name: 'Test Name',
    // Add other required fields with dummy data
    birthDate: DateTime(2010),
    age: 10,
    level: '1',
    experiencePoints: 100,
    experienceToNextLevel: 200,
    totalSteps: 5000,
    totalEnergyGenerated: 50,
    createdAt: DateTime(2023),
    lastActiveAt: DateTime(2023),
    isChild: true,
  );

  setUp(() {
    mockAuthBloc = MockAuthBloc();
    mockCardBloc = MockCardBloc();
    
    // Register CardBloc in GetIt since HomePage uses getIt<CardBloc> for navigation
    final getIt = GetIt.instance;
    if (getIt.isRegistered<CardBloc>()) {
      getIt.unregister<CardBloc>();
    }
    getIt.registerSingleton<CardBloc>(mockCardBloc);
  });
  
  tearDown(() {
     final getIt = GetIt.instance;
     if (getIt.isRegistered<CardBloc>()) {
      getIt.unregister<CardBloc>();
    }
  });

  Widget createWidgetUnderTest() {
    return MultiBlocProvider(
      providers: [
        BlocProvider<AuthBloc>.value(value: mockAuthBloc),
        // CardBloc is provided via GetIt in navigation, but if it's used in body, provide here too.
        // The code uses getIt<CardBloc> inside a push MaterialPageRoute builder, so the provider above 
        // doesn't affect the pushed route's context directly effectively until navigation happens, 
        // but the home page itself might rely on AuthBloc.
      ],
      child: MaterialApp(
        home: const HomePage(),
      ),
    );
  }

  testWidgets('HomePage displays loading indicator when state is initial/loading', (tester) async {
    when(() => mockAuthBloc.state).thenReturn(AuthInitial()); // Or AuthLoading if exists

    await tester.pumpWidget(createWidgetUnderTest());

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgets('HomePage displays guardian info when authenticated', (tester) async {
    when(() => mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: tGuardian));

    await tester.pumpWidget(createWidgetUnderTest());
    await tester.pump(); // Allow Text widgets to render

    expect(find.text('Hola, Test Name'), findsOneWidget);
    expect(find.text('@test_guardian'), findsOneWidget);
    expect(find.text('Nivel: 1'), findsOneWidget);
    expect(find.text('XP'), findsOneWidget); 
    // "100 para siguiente nivel" not "200" because the subtitle says: `${guardian.experienceToNextLevel} para siguiente nivel`
    // Wait, let me check the code:
    // value: guardian.experiencePoints.toString() -> '100'
    // subtitle: '${guardian.experienceToNextLevel} para siguiente nivel' -> '200 para siguiente nivel'
    
    expect(find.text('100'), findsOneWidget); // XP Value
    expect(find.text('200 para siguiente nivel'), findsOneWidget);
  });
  
  testWidgets('HomePage shows "Proximamente available" snackbar when tapping Battles', (tester) async {
    // Set a large enough surface size to ensure GridView items are visible
    tester.binding.window.physicalSizeTestValue = const Size(1080, 1920);
    tester.binding.window.devicePixelRatioTestValue = 1.0;

    when(() => mockAuthBloc.state).thenReturn(AuthAuthenticated(guardian: tGuardian));

    await tester.pumpWidget(createWidgetUnderTest());
    
    await tester.ensureVisible(find.text('Batallas'));
    await tester.tap(find.text('Batallas'));
    await tester.pump(); // Start animation
    await tester.pump(const Duration(milliseconds: 1000)); // Wait for snackbar animation
    
    expect(find.text('Próximamente disponible'), findsOneWidget);

    addTearDown(tester.binding.window.clearPhysicalSizeTestValue);
  });
}
