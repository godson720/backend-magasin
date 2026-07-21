import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

const String kApiBaseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'http://192.168.175.153:3000',
);

void main() {
  runApp(const MagasinApp());
}

class MagasinApp extends StatefulWidget {
  const MagasinApp({super.key});

  @override
  State<MagasinApp> createState() => _MagasinAppState();
}

class _MagasinAppState extends State<MagasinApp> {
  String? token;
  bool loading = true;

  @override
  void initState() {
    super.initState();
    _restoreToken();
  }

  Future<void> _restoreToken() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      token = prefs.getString('token');
      loading = false;
    });
  }

  Future<void> _saveToken(String newToken) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', newToken);
    setState(() => token = newToken);
  }

  Future<void> _clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
    setState(() => token = null);
  }

  @override
  Widget build(BuildContext context) {
    if (loading) {
      return const MaterialApp(home: Scaffold(body: Center(child: CircularProgressIndicator())));
    }

    return MaterialApp(
      title: 'Gestion Magasin',
      theme: ThemeData(colorSchemeSeed: Colors.indigo, useMaterial3: true),
      home: token == null
          ? LoginScreen(onLogin: _saveToken)
          : HomeScreen(token: token!, onLogout: _clearToken),
    );
  }
}

class LoginScreen extends StatefulWidget {
  final Future<void> Function(String token) onLogin;

  const LoginScreen({super.key, required this.onLogin});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final emailController = TextEditingController(text: 'admin@magasin.com');
  final passwordController = TextEditingController(text: 'Admin1234!');
  bool isLoading = false;
  String? errorMessage;

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final response = await http.post(
        Uri.parse('$kApiBaseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'email': emailController.text.trim(),
          'password': passwordController.text,
        }),
      );

      final body = jsonDecode(response.body);
      if (response.statusCode == 200 && body['token'] != null) {
        await widget.onLogin(body['token'] as String);
      } else {
        setState(() => errorMessage = body['message'] ?? 'Connexion impossible');
      }
    } catch (e) {
      setState(() => errorMessage = 'Impossible de joindre l’API : $e');
    } finally {
      setState(() => isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text('Gestion Magasin', style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  const Text('Connexion mobile', style: TextStyle(color: Colors.grey)),
                  const SizedBox(height: 24),
                  TextFormField(
                    controller: emailController,
                    decoration: const InputDecoration(labelText: 'Email', border: OutlineInputBorder()),
                    validator: (value) => value == null || value.isEmpty ? 'Email requis' : null,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: passwordController,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: 'Mot de passe', border: OutlineInputBorder()),
                    validator: (value) => value == null || value.isEmpty ? 'Mot de passe requis' : null,
                  ),
                  const SizedBox(height: 16),
                  if (errorMessage != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: Text(errorMessage!, style: const TextStyle(color: Colors.red)),
                    ),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: isLoading ? null : _submit,
                      child: isLoading ? const CircularProgressIndicator() : const Text('Se connecter'),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text('API : $kApiBaseUrl', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class HomeScreen extends StatefulWidget {
  final String token;
  final Future<void> Function() onLogout;

  const HomeScreen({super.key, required this.token, required this.onLogout});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late Future<List<dynamic>> productsFuture;
  late Future<Map<String, dynamic>> dashboardFuture;

  @override
  void initState() {
    super.initState();
    _refresh();
  }

  void _refresh() {
    setState(() {
      productsFuture = _fetchProducts();
      dashboardFuture = _fetchDashboard();
    });
  }

  Future<List<dynamic>> _fetchProducts() async {
    final response = await http.get(
      Uri.parse('$kApiBaseUrl/produits'),
      headers: {'Authorization': 'Bearer ${widget.token}'},
    );
    if (response.statusCode != 200) throw Exception('Impossible de charger les produits');
    final body = jsonDecode(response.body);
    return body is List ? body : body['data'] ?? [];
  }

  Future<Map<String, dynamic>> _fetchDashboard() async {
    final response = await http.get(
      Uri.parse('$kApiBaseUrl/dashboard'),
      headers: {'Authorization': 'Bearer ${widget.token}'},
    );
    if (response.statusCode != 200) throw Exception('Impossible de charger le tableau de bord');
    return jsonDecode(response.body) as Map<String, dynamic>;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Tableau de bord'),
        actions: [
          IconButton(icon: const Icon(Icons.logout), onPressed: () async => widget.onLogout()),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async => _refresh(),
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            FutureBuilder<Map<String, dynamic>>(
              future: dashboardFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Card(child: ListTile(title: Text('Chargement du tableau de bord...')));
                }
                if (snapshot.hasError) {
                  return Card(child: ListTile(title: Text('Erreur: ${snapshot.error}')));
                }
                final data = snapshot.data ?? {};
                return Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Résumé', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 12),
                        Text('Produits: ${data['totalProduits'] ?? 0}'),
                        Text('Ventes: ${data['totalVentes'] ?? 0}'),
                        Text('Revenue: ${data['montantTotal'] ?? 0}'),
                      ],
                    ),
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Produits', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                IconButton(onPressed: _refresh, icon: const Icon(Icons.refresh)),
              ],
            ),
            const SizedBox(height: 8),
            FutureBuilder<List<dynamic>>(
              future: productsFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return Text('Erreur : ${snapshot.error}');
                }
                final products = snapshot.data ?? [];
                if (products.isEmpty) {
                  return const Text('Aucun produit trouvé');
                }
                return Column(
                  children: products.map((product) {
                    final p = product as Map<String, dynamic>;
                    return Card(
                      child: ListTile(
                        title: Text(p['nom']?.toString() ?? 'Produit'),
                        subtitle: Text('Prix: ${p['prix'] ?? 0}'),
                        trailing: Text('${p['stock'] ?? 0} en stock'),
                      ),
                    );
                  }).toList(),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
